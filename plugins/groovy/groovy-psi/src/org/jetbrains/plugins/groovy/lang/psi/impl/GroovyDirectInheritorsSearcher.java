// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.groovy.lang.psi.impl;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.search.StubHierarchyInheritorSearcher;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopeUtil;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.DirectClassInheritorsSearch;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrAnonymousClassDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrReferenceList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.stubs.index.GrAnonymousClassIndex;
import org.jetbrains.plugins.groovy.lang.psi.stubs.index.GrDirectInheritorsIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author ven
 */
public class GroovyDirectInheritorsSearcher implements QueryExecutor<PsiClass, DirectClassInheritorsSearch.SearchParameters> {

  @NotNull
  private static List<PsiClass> getDerivingClassCandidates(PsiClass clazz, GlobalSearchScope scope, boolean includeAnonymous) {
    final String name = clazz.getName();
    if (name == null) return Collections.emptyList();
    final ArrayList<PsiClass> inheritors = new ArrayList<>();
    for (GrReferenceList list : StubIndex.getElements(GrDirectInheritorsIndex.KEY, name, clazz.getProject(), scope,
                                                      GrReferenceList.class)) {
      final PsiElement parent = list.getParent();
      if (parent instanceof GrTypeDefinition) {
        inheritors.add((PsiClass)parent);
      }
    }
    if (includeAnonymous) {
      final Collection<GrAnonymousClassDefinition> classes =
        StubIndex.getElements(GrAnonymousClassIndex.KEY, name, clazz.getProject(), scope, GrAnonymousClassDefinition.class);
      for (GrAnonymousClassDefinition aClass : classes) {
        inheritors.add(aClass);
      }
    }
    return inheritors;
  }

  @Override
  public boolean execute(@NotNull final DirectClassInheritorsSearch.SearchParameters queryParameters, @NotNull final Processor<? super PsiClass> consumer) {
    final PsiClass clazz = queryParameters.getClassToProcess();
    final SearchScope scope = queryParameters.getScope();
    Project project = PsiUtilCore.getProjectInReadAction(clazz);
    GlobalSearchScope globalSearchScope = GlobalSearchScopeUtil.toGlobalSearchScope(scope, project);
    final List<PsiClass> candidates = DumbService.getInstance(project).runReadActionInSmartMode(() -> {
      if (!clazz.isValid()) return Collections.emptyList();
      GlobalSearchScope restrictedScope = StubHierarchyInheritorSearcher.restrictScope(globalSearchScope);
      return getDerivingClassCandidates(clazz, restrictedScope, queryParameters.includeAnonymous());
    });
    for (final PsiClass candidate : candidates) {
      if (!queryParameters.isCheckInheritance() || isInheritor(clazz, candidate, project)) {
        if (!consumer.process(candidate)) {
          return false;
        }
      }
    }
    return true;
  }

  private static boolean isInheritor(PsiClass clazz, PsiClass candidate, Project project) {
    return DumbService.getInstance(project).runReadActionInSmartMode(() -> candidate.isValid() && candidate.isInheritor(clazz, false));
  }
}
