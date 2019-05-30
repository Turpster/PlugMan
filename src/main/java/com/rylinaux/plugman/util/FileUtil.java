package com.rylinaux.plugman.util;
/*
 * #%L
 * PlugMan
 * %%
 * Copyright (C) 2010 - 2019 PlugMan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import org.jetbrains.annotations.Nullable;

/**
 * Utilities to handle Files.
 */
public class FileUtil
{
    /**
     * Labels FileTypes in an enumeration.
     */
    public enum FileType
    {
        /**
         * Jarfiles
         */
        JAR_FILE("jar"),

        /**
         * Compressed zip files.
         */
        ZIP_FILE("zip");

        /**
         * The file extension.
         */
        final String extension;

        /**
         * Construct out object.
         * @param extension the extension.
         */
        FileType(String extension)
        {
            this.extension = extension;
        }

        /**
         * Export to string.
         * @return extension the extension with the dot.
         */
        @Override
        public String toString() {
            return "." + extension;
        }
    }

    /**
     * Get File Type of extension.
     * @param fileExtension the file extension
     * @return A File Type
     */
    @Nullable
    public static FileType getFileType(String fileExtension)
    {
        if (fileExtension.startsWith(".")) {
            fileExtension.substring(1);
        }

        fileExtension = fileExtension.toLowerCase();

        try {
            return FileType.valueOf(fileExtension);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}


