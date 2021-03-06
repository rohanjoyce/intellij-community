/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.intellij.build.images

import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.SVGLoader
import java.awt.Dimension
import java.awt.Image
import java.io.File
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO

internal val File.children: List<File> get() = if (isDirectory) listFiles().toList() else emptyList()

internal fun isImage(file: File, iconsOnly: Boolean): Boolean {
  if (!isImage(file)) return false
  return !iconsOnly || isIcon(file)
}

internal fun isIcon(file: File): Boolean {
  if (!isImage(file)) return false
  val size = imageSize(file) ?: return false
  return size.height == size.width || size.height <= 100 && size.width <= 100
}

private fun isImage(file: File) = ImageExtension.fromFile(file) != null

internal fun imageSize(file: File): Dimension? {
  val image: Image = loadImage(file) ?: return null
  val width = image.getWidth(null)
  val height = image.getHeight(null)
  return Dimension(width, height)
}

internal fun loadImage(file: File): Image? {
  if (file.name.endsWith(".svg")) {
    try {
      return SVGLoader.load(URL("file://" + file.path), 1.0f)
    }
    catch (e: IOException) {
      return null
    }
  }
  else {
    return ImageIO.read(file)
  }
}

internal enum class ImageType(private val suffix: String) {
  BASIC(""), RETINA("@2x"), DARCULA("_dark"), RETINA_DARCULA("@2x_dark");

  companion object {
    fun getBasicName(file: File, prefix: List<String>): String {
      val name = FileUtil.getNameWithoutExtension(file.name)
      return stripSuffix((prefix + name).joinToString("/"))
    }

    fun fromFile(file: File): ImageType {
      val name = FileUtil.getNameWithoutExtension(file.name)
      return fromName(name)
    }

    fun fromName(name: String): ImageType {
      if (name.endsWith(RETINA_DARCULA.suffix)) return RETINA_DARCULA
      if (name.endsWith(RETINA.suffix)) return RETINA
      if (name.endsWith(DARCULA.suffix)) return DARCULA
      return BASIC
    }

    fun stripSuffix(name: String): String {
      val type = fromName(name)
      return name.removeSuffix(type.suffix)
    }
  }
}

internal enum class ImageExtension(private val suffix: String) {
  PNG(".png"), SVG(".svg"), GIF(".gif");

  companion object {
    fun fromFile(file: File): ImageExtension? {
      return fromName(file.name)
    }

    fun fromName(name: String): ImageExtension? {
      if (name.endsWith(PNG.suffix)) return PNG
      if (name.endsWith(SVG.suffix)) return SVG
      if (name.endsWith(GIF.suffix)) return GIF
      return null
    }
  }
}