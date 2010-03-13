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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/operators/LogicalAndTest.java $
 $Id: LogicalAndTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.dml.operators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quattor.pan.dml.AbstractOperationTestUtils;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.HashResource;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.exceptions.SyntaxException;

public class LogicalAndTest extends AbstractOperationTestUtils {

	@Test
	public void testBooleans1() throws SyntaxException {

		boolean va = true;
		boolean vb = true;

		// Execute the operations.
		Operation land = LogicalAnd.newOperation(null, BooleanProperty
				.getInstance(va), BooleanProperty.getInstance(vb));

		// Check result.
		assertTrue(land instanceof BooleanProperty);
		BooleanProperty result = (BooleanProperty) land;
		boolean bvalue = result.getValue().booleanValue();
		assertTrue((va && vb) == bvalue);
	}

	@Test
	public void testBooleans2() throws SyntaxException {

		boolean va = true;
		boolean vb = false;

		// Execute the operations.
		Operation land = LogicalAnd.newOperation(null, BooleanProperty
				.getInstance(va), BooleanProperty.getInstance(vb));

		// Check result.
		assertTrue(land instanceof BooleanProperty);
		BooleanProperty result = (BooleanProperty) land;
		boolean bvalue = result.getValue().booleanValue();
		assertTrue((va && vb) == bvalue);
	}

	@Test
	public void testBooleans3() throws SyntaxException {

		boolean va = false;
		boolean vb = true;

		// Execute the operations.
		Operation land = LogicalAnd.newOperation(null, BooleanProperty
				.getInstance(va), BooleanProperty.getInstance(vb));

		// Check result.
		assertTrue(land instanceof BooleanProperty);
		BooleanProperty result = (BooleanProperty) land;
		boolean bvalue = result.getValue().booleanValue();
		assertTrue((va && vb) == bvalue);
	}

	@Test
	public void testBooleans4() throws SyntaxException {

		boolean va = false;
		boolean vb = false;

		// Execute the operations.
		Operation land = LogicalAnd.newOperation(null, BooleanProperty
				.getInstance(va), BooleanProperty.getInstance(vb));

		// Check result.
		assertTrue(land instanceof BooleanProperty);
		BooleanProperty result = (BooleanProperty) land;
		boolean bvalue = result.getValue().booleanValue();
		assertTrue((va && vb) == bvalue);
	}

	@Test
	public void testShortCircuit1() throws SyntaxException {

		boolean va = false;
		long vb = 1L;

		// Execute the operations. If the second argument is evaluated, then an
		// exception will be thrown.
		Operation land = LogicalAnd.newOperation(null, BooleanProperty
				.getInstance(va), LongProperty.getInstance(vb));

		// Check result.
		assertTrue(land instanceof BooleanProperty);
		BooleanProperty result = (BooleanProperty) land;
		boolean bvalue = result.getValue().booleanValue();
		assertFalse(bvalue);
	}

	@Test(expected = SyntaxException.class)
	public void testShortCircuit2() throws SyntaxException {

		boolean va = true;
		long vb = 1L;

		// Execute the operations. If the second argument is evaluated, then an
		// exception will be thrown.
		LogicalAnd.newOperation(null, BooleanProperty.getInstance(va),
				LongProperty.getInstance(vb));
	}

	@Test(expected = AssertionError.class)
	public void testUnderflow() throws SyntaxException {
		LogicalAnd.newOperation(null);
	}

	@Test(expected = AssertionError.class)
	public void testOverflow() throws SyntaxException {
		BooleanProperty b = BooleanProperty.TRUE;
		LogicalAnd.newOperation(null, b, b, b);
	}

	@Test(expected = SyntaxException.class)
	public void testInvalidArgument() throws SyntaxException {
		HashResource h = new HashResource();
		LogicalAnd.newOperation(null, h, h);
	}
}
