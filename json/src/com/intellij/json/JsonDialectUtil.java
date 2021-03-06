// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.json;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class JsonDialectUtil {
  public static boolean isStandardJson(@NotNull PsiElement element) {
    return getLanguage(element) == JsonLanguage.INSTANCE;
  }

  public static Language getLanguage(@NotNull PsiElement element) {
    PsiFile file = element.getContainingFile();
    if (file == null) return JsonLanguage.INSTANCE;
    Language language = file.getLanguage();
    return language instanceof JsonLanguage ? language : JsonLanguage.INSTANCE;
  }
}
