package test.java.util;

/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


import java.util.Random;
 
/**
 * Collection utilities for generating random strings of symbols.
 * @version     %I%, %G%
 */
public class StringUtilities {  
    private final static Random RANDOM =  new Random(System.currentTimeMillis());
    
    /**
     * Generates random string using specified parameters.
     * 
     * @param count     length of resulting string
     * @param start     number of starting character 
     *                  in chars array. If both start 
     *                  and end are zero then used 
     *                  chars include all letters, numbers
     *                  and punctuation marks.
     * @param end       number of end character 
     *                  in chars array. If both start 
     *                  and end are zero then used 
     *                  chars include all letters, numbers
     *                  and punctuation marks.
     * @param letters   <code>true</code> if letters should
     *                  be used in generating of the string
     * @param numbers   <code>true</code> if numbers should
     *                  be used in generating of the string
     * @param chars     char[] array of chars to use. If <code>null</code> then
     *                  ASCII code table is used.
     * @param rnd       Random instance used to generate chars.
     * @see             java.util.Random
     * @return          String with randomly generated content
     */
    public static String random(int count, int start, int end, boolean letters, 
            boolean numbers, char[] chars, Random rnd) {
        if (count == 0) {
            return "";
        } else if (count < 0) {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }
        if ((start == 0) && (end == 0)) {
            end = 'z' + 1;
            start = ' ';
            if (!letters && !numbers) {
                start = 0;
                end = Integer.MAX_VALUE;
            }
        }

        char[] buffer = new char[count];
        int gap = end - start;

        while (count-- != 0) {
            char ch;
            if (chars == null) {
                ch = (char) (rnd.nextInt(gap) + start);
            } else {
                ch = chars[rnd.nextInt(gap) + start];
            }
            if ((letters && Character.isLetter(ch))
                || (numbers && Character.isDigit(ch))
                || (!letters && !numbers)) 
            {
                if(ch >= 56320 && ch <= 57343) {
                    if(count == 0) {
                        count++;
                    } else {
                        // low surrogate, insert high surrogate after putting it in
                        buffer[count] = ch;
                        count--;
                        buffer[count] = (char) (55296 + rnd.nextInt(128));
                    }
                } else if(ch >= 55296 && ch <= 56191) {
                    if(count == 0) {
                        count++;
                    } else {
                        // high surrogate, insert low surrogate before putting it in
                        buffer[count] = (char) (56320 + rnd.nextInt(128));
                        count--;
                        buffer[count] = ch;
                    }
                } else if(ch >= 56192 && ch <= 56319) {
                    // private high surrogate, no effing clue, so skip it
                    count++;
                } else {
                    buffer[count] = ch;
                }
            } else {
                count++;
            }
        }
        return new String(buffer);
    }
    
    /**
     * Generates random string using specified parameters.
     * 
     * @param count     length of resulting string
     * @param letters   <code>true</code> if letters should
     *                  be used in generating of the string
     * @param numbers   <code>true</code> if numbers should
     *                  be used in generating of the string
     * @return          String with randomly generated content
     */
    public static String random(int count, boolean letters, boolean numbers) {
        return random(count, 0, 0, letters, numbers);
    }
    
    /**
     * Generates random string using specified parameters.
     * 
     * @param count     length of resulting string
     * @param start     number of starting character 
     *                  in chars array. If both start 
     *                  and end are zero then used 
     *                  chars include all letters, numbers
     *                  and punctuation marks.
     * @param end       number of end character 
     *                  in chars array. If both start 
     *                  and end are zero then used 
     *                  chars include all letters, numbers
     *                  and punctuation marks.
     * @param letters   <code>true</code> if letters should
     *                  be used in generating of the string
     * @param numbers   <code>true</code> if numbers should
     *                  be used in generating of the string
     * @see             java.util.Random
     * @return          String with randomly generated content
     */
    public static String random(int count, int start, int end, boolean letters, boolean numbers) {
        return random(count, start, end, letters, numbers, null, RANDOM);
    }

    /**
     * Generates random string using specified parameters.
     * 
     * @param count     length of resulting string
     * @param start     number of starting character 
     *                  in chars array. If both start 
     *                  and end are zero then used 
     *                  chars include all letters, numbers
     *                  and punctuation marks.
     * @param end       number of end character 
     *                  in chars array. If both start 
     *                  and end are zero then used 
     *                  chars include all letters, numbers
     *                  and punctuation marks.
     * @param letters   <code>true</code> if letters should
     *                  be used in generating of the string
     * @param numbers   <code>true</code> if numbers should
     *                  be used in generating of the string
     * @param chars     char[] array of chars to use. If <code>null</code> then
     *                  ASCII code table is used.
     * @see             java.util.Random
     * @return          String with randomly generated content
     */
    public static String random(int count, int start, int end, boolean letters, boolean numbers, char[] chars) {
        return random(count, start, end, letters, numbers, chars, RANDOM);
    }
    
    /**
     * Generates string of random length with random content.
     * 
     * @param max_length    maximal length of resulting string
     * @param min_length    minimal length of resulting string
     * @return              String with randomly generated content
     *                      containing letters, numbers and punctuation symbols
     */
    public static String randomString(int max_length, int min_length){
        return randomAscii(min_length + RANDOM.nextInt(max_length - min_length));
    }    
    
    /**
     *  Generates string of random length with random content.
     * @param count         length of resulting string
     * @return              String of random content
     */
    public static String random(int count) {
        return random(count, false, false);
    }

    /**
     *  Generates string of random length with random content
     *  using all visible ASCII symbols.
     * @param count         length of resulting string
     * @return              String of random content
     */
    public static String randomAscii(int count) {
        return random(count, 32, 127, false, false);
    }

    /**
     *  Generates string of random length with random content
     *  using only letters.
     * @param count         length of resulting string
     * @return              String of random content
     */
    public static String randomAlphabetic(int count) {
        return random(count, true, false);
    }
    
    /**
     *  Generates string of random length with random content
     *  using letters and numbers.
     * @param count         length of resulting string
     * @return              String of random content
     */
    public static String randomAlphanumeric(int count) {
        return random(count, true, true);
    }

    /**
     *  Generates string of random length with random content
     *  using numbers.
     * @param count         length of resulting string
     * @return              String of random content
     */
    public static String randomNumeric(int count) {
        return random(count, false, true);
    }
}

