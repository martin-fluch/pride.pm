package basic;
import java.sql.SQLException;

import org.junit.Test;

/*******************************************************************************
 * Copyright (c) 2001-2007 The PriDE team and MATHEMA Software GmbH
 * All rights reserved. This toolkit and the accompanying materials 
 * are made available under the terms of the GNU Lesser General Public
 * License (LGPL) which accompanies this distribution, and is available
 * at http://pride.sourceforge.net/LGPL.html
 * 
 * Contributors:
 *     Jan Lessner, MATHEMA Software GmbH - JUnit test suite
 *******************************************************************************/
import de.mathema.pride.DatabaseFactory;
import de.mathema.pride.WhereCondition;

/**
 * @author bart57
 *
 * Class to Test the Update-Behaviour of the PriDE-Framework
 */
public class PrideUpdateTest extends AbstractPrideTest {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		generateCustomer(9);
		WhereCondition.setBindDefault(false);
	}

	@Test
	public void testUpdatePK() throws Exception{
		Customer c = createCustomer(1);
		assertEquals("First", c.getFirstName());
		assertEquals("Customer", c.getLastName());
		c.setFirstName("Casper");
		c.setLastName("Kopp");
		c.update();
		DatabaseFactory.getDatabase().commit();
		Customer c2 = new Customer(1);
		assertEquals("Casper", c2.getFirstName());
		assertEquals("Kopp", c2.getLastName());
	}

	@Test
	public void testUpdateByExample() throws Exception{
		Customer c = createCustomer(1);
		assertEquals("First", c.getFirstName());
		assertEquals("Customer", c.getLastName());
		c.setFirstName("Inge");
		c.setLastName("Updated");
		c.update(new String[] { "id" });
		DatabaseFactory.getDatabase().commit();
		Customer[] ca = (Customer[])c.query(new String [] { "lastName" } ).toArray();
		assertEquals(ca.length, 1);
		assertEquals("Inge", ca[0].getFirstName());
		assertEquals("Updated", ca[0].getLastName());
	}

	@Test
	public void testUpdateFields() throws Exception{
		Customer c = createCustomer(1);
		assertEquals("First", c.getFirstName());
		assertEquals("Customer", c.getLastName());
		c.setFirstName("Casper");
		c.update((String[])null, new String[] { "firstName" });
		DatabaseFactory.getDatabase().commit();
		Customer c2 = new Customer(1);
		assertEquals("Casper", c2.getFirstName());
		assertEquals("Customer", c2.getLastName());
	}

	@Test
	public void testUpdateMultiple() throws Exception{
		Customer c = createCustomer();
		c.setFirstName("Inge");
		c.setLastName("Updated");
		c.update(new String[] { "firstName"}, new String[] { "lastName" });
		DatabaseFactory.getDatabase().commit();
		Customer[] ca = (Customer[])c.query(new String [] { "firstName" } ).toArray();
		assertEquals(ca.length, 2);
		assertTrue(ca[0].getId() != ca[1].getId());
		assertTrue(ca[0].getId() == 7 || ca[0].getId() == 8);
		assertTrue(ca[1].getId() == 7 || ca[1].getId() == 8);
	}

	@Test
	public void testUpdateWhere() throws Exception{
		Customer c = createCustomer();
		c.setLastName("Updated");
		c.update(new WhereCondition().and("firstName", WhereCondition.Operator.LIKE, "Pe%"), new String[] { "lastName" });
		DatabaseFactory.getDatabase().commit();
		Customer[] ca = (Customer[])c.query(new String [] { "lastName" } ).toArray();
		assertEquals(ca.length, 2);
		assertTrue(ca[0].getId() != ca[1].getId());
		assertTrue(ca[0].getId() == 2 || ca[0].getId() == 5);
		assertTrue(ca[1].getId() == 2 || ca[1].getId() == 5);
	}

	@Test
	public void testUpdatePlain() throws Exception {
		String update = "update " + TEST_TABLE + " set firstName='Updated' where lastName='Customer'";
		int numUpdates = DatabaseFactory.getDatabase().sqlUpdate(update);
		assertEquals(2, numUpdates);
	}
	
	@Test
	public void testUpdatePlainPrepared() throws Exception {
		String update = "update " + TEST_TABLE + " set firstName=? where lastName=?";
		int numUpdates = DatabaseFactory.getDatabase().sqlUpdate(update, "Updated", "Customer");
		assertEquals(2, numUpdates);
	}
	
	protected Customer createCustomer() {
		return new Customer();
	}

	protected Customer createCustomer(int id) throws SQLException {
		return new Customer(id);
	}

}
