/*
 * The MIT License
 *
 * Copyright (c) 2010-2016 Workable SA
 *
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
 */

package com.workable.errorhandler;

/**
 * Container to ease passing around a tuple of two objects. This object provides a sensible
 * implementation of equals(), returning true if equals() is true on each of the contained
 * objects.
 */
public class ActionEntry {

    public final Matcher matcher;
    public final Action action;

    /**
     * Constructor for an ActionEntry.
     *
     * @param matcher the matcher object in the ActionEntry
     * @param action  the action object in the ActionEntry
     */
    public ActionEntry(Matcher matcher, Action action) {
        this.matcher = matcher;
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionEntry that = (ActionEntry) o;

        if (!matcher.equals(that.matcher)) return false;
        return action.equals(that.action);
    }

    /**
     * Compute a hash code using the hash codes of the underlying objects
     *
     * @return a hashcode of the ActionEntry
     */
    @Override
    public int hashCode() {
        return (matcher == null ? 0 : matcher.hashCode()) ^ (action == null ? 0 : action.hashCode());
    }

    /**
     * Convenience method for creating an ActionEntry
     *
     * @param matcher the matcher object in the ActionEntry
     * @param action  the action object in the ActionEntry
     */
    public static ActionEntry from(Matcher matcher, Action action) {
        return new ActionEntry(matcher, action);
    }
}

