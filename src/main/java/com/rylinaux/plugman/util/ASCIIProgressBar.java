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


/**
 * Simple ASCII progress bar
 *
 * @author turpster
 */
public class ASCIIProgressBar
{
    /**
     * starting ASCII border.
     */
    public static final char START_BORDER = '[';

    /**
     * ending ASCII border.
     */
    public static final char END_BORDER = ']';

    /**
     * char to represent the numerator.
     */
    public static final char DOWNLOADED = '#';

    /**
     * char to represent the denominator.
     */
    public static final char NOT_DOWNLOADED = '-';

    /**
     * percentage of the progress bar.
     */
    float percentage = 0;

    /**
     * A setter for the percentage.
     *
     * @param percentage new percentage.
     */
    public void setPercentage(float percentage)
    {
        if (percentage < 0)
        {
            this.percentage = 0;
        }
        else if (percentage > 1)
        {
            this.percentage = 1;
        }
        else
        {
            this.percentage = percentage;
        }
    }

    /**
     * Getter for the ASCII progress bar.
     *
     * @param charWidth the string width of the progress bar.
     * @return the ascii progress bar.
     */
    public String getProgressBar(short charWidth)
    {
        StringBuilder progressBar = new StringBuilder();
        charWidth -= 2;

        progressBar.append(START_BORDER);

        for (int i = 0; i < Math.floor(charWidth * percentage); i++)
        {
            progressBar.append(DOWNLOADED);
            charWidth--;
        }

        for (int i = 0; i < charWidth; i++)
        {
            progressBar.append(NOT_DOWNLOADED);
        }

        progressBar.append(END_BORDER);

        return progressBar.toString();
    }
}
