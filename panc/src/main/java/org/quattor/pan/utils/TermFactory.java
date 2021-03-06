/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/Sources/panc/trunk/src/org/quattor/pan/utils/Term.java $
 $Id: Term.java 1000 2006-11-15 20:47:58Z loomis $
 */

package org.quattor.pan.utils;

import static org.quattor.pan.utils.MessageUtils.MSG_INDEX_CANNOT_BE_NEGATIVE;
import static org.quattor.pan.utils.MessageUtils.MSG_INDEX_EXCEEDS_MAXIMUM;
import static org.quattor.pan.utils.MessageUtils.MSG_INDEX_BELOW_MINIMUM;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_ELEMENT_FOR_INDEX;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_LEADING_ZEROS_INDEX;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_KEY;
import static org.quattor.pan.utils.MessageUtils.MSG_KEY_CANNOT_BEGIN_WITH_DIGIT;
import static org.quattor.pan.utils.MessageUtils.MSG_KEY_CANNOT_BE_EMPTY_STRING;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;

/**
 * A factory to generate Term objects. The produced objects are just standard
 * StringProperty and LongProperty instances.
 *
 * @author loomis
 *
 */
public class TermFactory {

	/**
	 * This regular expression identifies strings which might be a list index. (That
	 * is, they are digits only (and optional minus sign.)
	 */
	private static final Pattern isIndexPattern = Pattern.compile("^-?\\d+$"); //$NON-NLS-1$

	/**
	 * This regular expression identifies indicies with leading zeros (incl optional minus sign).
	 */
	private static final Pattern isIndexLeadingZerosPattern = Pattern.compile("^-?0\\d+$"); //$NON-NLS-1$

	/**
	 * This regular expression identifies valid dict keys.
	 */
	private static final Pattern isKeyPattern = Pattern
			.compile("^\\w[\\w\\+\\-\\.]*$"); //$NON-NLS-1$

    // ConcurrentHashMap is ok for checkStringIndex
    //  2 threads will get the same result,
    //  so it's not an issue if they add the element
    private static Map<String, Term> createStringCache = new ConcurrentHashMap<String, Term>(50000);

    private static Map<Long, Term> createLongCache = new ConcurrentHashMap<Long, Term>(1000);

	private TermFactory() {
	}

	/**
	 * An internal method to check that a numeric index is valid. It must be a
	 * non-negative integer.
	 *
	 * @param index
	 * @return validated index as an Integer
	 */
	private static void checkNumericIndex(long index) {
		if (index > Integer.MAX_VALUE) {
			throw EvaluationException.create(MSG_INDEX_EXCEEDS_MAXIMUM, index,
					Integer.MAX_VALUE);
		} else if (index < Integer.MIN_VALUE) {
			throw EvaluationException.create(MSG_INDEX_BELOW_MINIMUM, index,
					Integer.MIN_VALUE);
		}
	}

	/**
	 * An internal method to check that a string value is valid. It can either
	 * be an Integer or String which is returned.
	 *
	 * @param term
	 * @return validated term as Integer or String
	 */
	private static long[] checkStringIndex(String term) {

		assert (term != null);

        // If second element is < 0, this is a dict key
		long[] result = {0L, 1L};

		// Empty strings are not allowed.
		if ("".equals(term)) { //$NON-NLS-1$
			throw EvaluationException.create(MSG_KEY_CANNOT_BE_EMPTY_STRING);
		}

		if (isIndexPattern.matcher(term).matches()) {
            if (isIndexLeadingZerosPattern.matcher(term).matches()) {
				throw EvaluationException.create(
						MSG_INVALID_LEADING_ZEROS_INDEX, term);
            } else {
                // This is digits only, so try to convert it to a long.
                try {
                    result[0] = Long.decode(term).longValue();
                    checkNumericIndex(result[0]);
                } catch (NumberFormatException nfe) {
                    throw EvaluationException.create(
						MSG_KEY_CANNOT_BEGIN_WITH_DIGIT, term);
                }
            }
		} else if (isKeyPattern.matcher(term).matches()) {

			// Return a negative number to indicate that this is an OK key
			// value.
			result[1] = -1L;

		} else {

			// Doesn't work for either a key or index.
			throw EvaluationException.create(MSG_INVALID_KEY, term);
		}

		return result;
	}

	/**
	 * Constructor of a path from a String. If the path does not have the
	 * correct syntax, an IllegalArgumentException will be thrown.
	 */
	public static Term create(String term) {
        Term res = createStringCache.get(term);

        if (res == null) {
            if (term != null && term.length() >= 2 &&
                term.charAt(0) == '{' && term.charAt(term.length() - 1) == '}') {
                term = EscapeUtils.escape(term.substring(1, term.length() - 1));
            }

            long[] value = checkStringIndex(term);
            if (value[1] < 0L) {
                res = (Term) StringProperty.getInstance(term);
            } else {
                res = (Term) create(value[0]);
            }
            createStringCache.put(term, res);
        }

        // no clone/copy needed for cached result
        return res;
	}

	/**
	 * Create a term directly from a long index.
	 *
	 * @param index
	 *            the index to use for this term
	 */
	public static Term create(long index) {
        Term res = createLongCache.get(index);
        if (res == null) {
            checkNumericIndex(index);

            // Generate a new property.
            res = (Term) LongProperty.getInstance(index);
            createLongCache.put(index, res);
            return res;
        }

        // no clone/copy needed for cached result
        return res;
	}

	/**
	 * Create a term from a given element.
	 *
	 * @param element
	 *            the element to create the term from
	 */
	public static Term create(Element element) {
		if (element instanceof StringProperty) {
            return create(((StringProperty) element).getValue());
		} else if (element instanceof LongProperty) {
			return create(((LongProperty) element).getValue().longValue());
		} else {
			throw EvaluationException.create(MSG_INVALID_ELEMENT_FOR_INDEX,
					element.getTypeAsString());
		}
	}

	/**
	 * A utility method to allow the comparison of any two terms.
	 */
	public static int compare(Term self, Term other) {

		// Sanity check.
		if (self == null || other == null) {
			throw new NullPointerException();
		}

		// Identical objects are always equal.
		if (self == other) {
			return 0;
		}

		// Easy case is when they are not the same type of term.
		if (self.isKey() != other.isKey()) {
			return (self.isKey()) ? 1 : -1;
		}

		// Compare the underlying values.
		try {
			if (self.isKey()) {
				return self.getKey().compareTo(other.getKey());
			} else {
				return self.getIndex().compareTo(other.getIndex());
			}
		} catch (InvalidTermException consumed) {
			// This statement can never be reached because both objects are
			// either keys or indexes. This try/catch block is only here to make
			// the compiler happy.
			assert (false);
			return 0;
		}
	}

}
