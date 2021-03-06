// Copyright (c) 2003-present, Jodd Team (http://jodd.org)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package jodd.db.oom.sqlgen;

import jodd.db.DbOom;
import jodd.db.oom.DbEntityManager;
import jodd.db.oom.fixtures.BadBoy;
import jodd.db.oom.fixtures.BadGirl;
import jodd.db.oom.fixtures.Boy;
import jodd.db.oom.fixtures.Girl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DbEntitySqlTest {

	private DbOom dbOom;

	@BeforeEach
	void setUp() {
		dbOom = DbOom.create().get();
		DbEntityManager dbEntityManager = DbOom.get().entityManager();

		dbEntityManager.registerType(Boy.class);
		dbEntityManager.registerType(BadBoy.class);
		dbEntityManager.registerType(BadGirl.class);
		dbEntityManager.registerType(Girl.class);
	}

	@AfterEach
	void tearDown() {
		DbOom.get().shutdown();
	}

	protected void checkGirl(DbSqlBuilder b) {
		Map<String, ParameterValue> params = b.getQueryParameters();
		assertEquals(3, params.entrySet().size());
		assertEquals(Integer.valueOf(1), params.get("girl.id").getValue());
		assertEquals("sanja", params.get("girl.name").getValue());
		assertEquals("c++", params.get("girl.speciality").getValue());
	}

	protected void checkBadGirl1(DbSqlBuilder b) {
		Map<String, ParameterValue> params = b.getQueryParameters();
		assertEquals(2, params.entrySet().size());
		assertEquals(Integer.valueOf(2), params.get("badGirl.fooid").getValue());
		assertEquals(".net", params.get("badGirl.foospeciality").getValue());
	}

	protected void checkBadGirl2(DbSqlBuilder b) {
		Map<String, ParameterValue> params = b.getQueryParameters();
		assertEquals(3, params.entrySet().size());
		assertEquals(Integer.valueOf(2), params.get("badGirl.fooid").getValue());
		assertEquals(".net", params.get("badGirl.foospeciality").getValue());
		assertNull(params.get("badGirl.fooname").getValue());
	}

	protected void checkBadGirl3(DbSqlBuilder b) {
		Map<String, ParameterValue> params = b.getQueryParameters();
		assertEquals(1, params.entrySet().size());
		assertEquals(Integer.valueOf(2), params.get("badGirl.fooid").getValue());
	}

	protected void checkBadGirl4(DbSqlBuilder b) {
		Map<String, ParameterValue> params = b.getQueryParameters();
		assertEquals(1, params.entrySet().size());
		assertEquals("2", params.get("p0").getValue().toString());
	}

	protected void checkGirl1(DbSqlBuilder b) {
		Map<String, ParameterValue> params = b.getQueryParameters();
		assertEquals(1, params.entrySet().size());
		assertEquals("sanja", params.get("p0").getValue());
	}

	@Test
	void testInsert() {
		Girl g = new Girl(1, "sanja", "c++");
		DbSqlBuilder b = dbOom.entities().insert(g);
		assertEquals("insert into GIRL (ID, NAME, SPECIALITY) values (:girl.id, :girl.name, :girl.speciality)", b.generateQuery());
		checkGirl(b);
	}

	@Test
	void testTruncate() {
		Girl g = new Girl(1, "sanja", "c++");
		assertEquals("delete from GIRL", dbOom.entities().truncate(g).generateQuery());
		assertEquals("delete from GIRL", dbOom.entities().truncate(Girl.class).generateQuery());
	}

	@Test
	void testUpdate() {
		Girl g = new Girl(1, "sanja", "c++");
		DbSqlBuilder b = dbOom.entities().update(g);
		assertEquals("update GIRL Girl_ set ID=:girl.id, NAME=:girl.name, SPECIALITY=:girl.speciality  where (1=1)",
				b.generateQuery());
		checkGirl(b);

		BadGirl bg = new BadGirl(Integer.valueOf(2), null, ".net");
		b = dbOom.entities().update(bg);
		assertEquals(
				"update GIRL BadGirl_ set ID=:badGirl.fooid, SPECIALITY=:badGirl.foospeciality  where (BadGirl_.ID=:badGirl.fooid)",
				b.generateQuery());
		checkBadGirl1(b);

		b = dbOom.entities().updateAll(bg);
		assertEquals(
				"update GIRL BadGirl_ set ID=:badGirl.fooid, NAME=:badGirl.fooname, SPECIALITY=:badGirl.foospeciality  where (BadGirl_.ID=:badGirl.fooid)",
				b.generateQuery());
		checkBadGirl2(b);
	}

	@Test
	void testUpdateColumn() {
		BadGirl bg = new BadGirl(Integer.valueOf(1), "sanja", "c++");
		DbSqlBuilder b = dbOom.entities().updateColumn(bg, "fooname", "Anja");
		assertEquals(
				"update GIRL BadGirl_ set NAME=:p0 where (BadGirl_.ID=:badGirl.fooid)",
				b.generateQuery());
		Map<String, ParameterValue> params = b.getQueryParameters();
		assertEquals(2, params.entrySet().size());
		assertEquals(Integer.valueOf(1), params.get("badGirl.fooid").getValue());
		assertEquals("Anja", params.get("p0").getValue());
	}

	@Test
	void testDelete() {
		Girl g = new Girl(1, "sanja", "c++");
		DbSqlBuilder b = dbOom.entities().delete(g);
		assertEquals("delete from GIRL where (GIRL.ID=:girl.id and GIRL.NAME=:girl.name and GIRL.SPECIALITY=:girl.speciality)",
				b.generateQuery());
		checkGirl(b);

		BadGirl bg = new BadGirl(Integer.valueOf(2), null, ".net");
		b = dbOom.entities().delete(bg);
		assertEquals(
				"delete from GIRL where (GIRL.ID=:badGirl.fooid and GIRL.SPECIALITY=:badGirl.foospeciality)",
				b.generateQuery());
		checkBadGirl1(b);

		b = dbOom.entities().deleteByAll(bg);
		assertEquals(
				"delete from GIRL where (GIRL.ID=:badGirl.fooid and GIRL.NAME=:badGirl.fooname and GIRL.SPECIALITY=:badGirl.foospeciality)",
				b.generateQuery());
		checkBadGirl2(b);

		b = dbOom.entities().deleteById(bg);
		assertEquals(
				"delete from GIRL where (GIRL.ID=:badGirl.fooid)",
				b.generateQuery());
		checkBadGirl3(b);

		b = dbOom.entities().deleteById(bg, 2);
		assertEquals(
				"delete from GIRL where GIRL.ID=:p0",
				b.generateQuery());
		checkBadGirl4(b);
	}

	@Test
	void testFrom() {
		Girl g = new Girl(1, "sanja", "c++");

		assertEquals("select Girl_.ID, Girl_.NAME, Girl_.SPECIALITY from GIRL Girl_ ",
				dbOom.entities().from(g).generateQuery());

		assertEquals("select BadGirl_.ID, BadGirl_.NAME, BadGirl_.SPECIALITY from GIRL BadGirl_ ",
				dbOom.entities().from(BadGirl.class).generateQuery());

		assertEquals("select ggg.ID, ggg.NAME, ggg.SPECIALITY from GIRL ggg ",
				dbOom.entities().from(BadGirl.class, "ggg").generateQuery());
	}

	@Test
	void testFind() {
		Girl g = new Girl(1, "sanja", "c++");
		DbSqlBuilder b = dbOom.entities().find(g);
		assertEquals("select Girl_.ID, Girl_.NAME, Girl_.SPECIALITY from GIRL Girl_ where (Girl_.ID=:girl.id and Girl_.NAME=:girl.name and Girl_.SPECIALITY=:girl.speciality)",
				b.generateQuery());
		checkGirl(b);

		b = dbOom.entities().findByAll(g);
		assertEquals("select Girl_.ID, Girl_.NAME, Girl_.SPECIALITY from GIRL Girl_ where (Girl_.ID=:girl.id and Girl_.NAME=:girl.name and Girl_.SPECIALITY=:girl.speciality)",
				b.generateQuery());
		checkGirl(b);

		BadGirl bg = new BadGirl(Integer.valueOf(2), null, ".net");
		b = dbOom.entities().find(bg);
		assertEquals("select BadGirl_.ID, BadGirl_.NAME, BadGirl_.SPECIALITY from GIRL BadGirl_ where (BadGirl_.ID=:badGirl.fooid and BadGirl_.SPECIALITY=:badGirl.foospeciality)",
				b.generateQuery());
		checkBadGirl1(b);

		b = dbOom.entities().findByAll(bg);
		assertEquals("select BadGirl_.ID, BadGirl_.NAME, BadGirl_.SPECIALITY from GIRL BadGirl_ where (BadGirl_.ID=:badGirl.fooid and BadGirl_.NAME=:badGirl.fooname and BadGirl_.SPECIALITY=:badGirl.foospeciality)",
				b.generateQuery());
		checkBadGirl2(b);

		b = dbOom.entities().findByColumn(Girl.class, "name", "sanja");
		assertEquals("select Girl_.ID, Girl_.NAME, Girl_.SPECIALITY from GIRL Girl_ where Girl_.NAME=:p0",
				b.generateQuery());
		checkGirl1(b);

		b = dbOom.entities().findByColumn(BadGirl.class, "fooname", "sanja");
		assertEquals("select BadGirl_.ID, BadGirl_.NAME, BadGirl_.SPECIALITY from GIRL BadGirl_ where BadGirl_.NAME=:p0",
				b.generateQuery());
		checkGirl1(b);

		b = dbOom.entities().findForeign(BadBoy.class, bg);
		assertEquals("select BadBoy_.ID, BadBoy_.GIRL_ID, BadBoy_.NAME from BOY BadBoy_ where BadBoy_.GIRL_ID=:p0",
				b.generateQuery());
		checkBadGirl4(b);

		b = dbOom.entities().findById(bg);
		assertEquals("select BadGirl_.ID, BadGirl_.NAME, BadGirl_.SPECIALITY from GIRL BadGirl_ where (BadGirl_.ID=:badGirl.fooid)",
				b.generateQuery());
		checkBadGirl3(b);

		b = dbOom.entities().findById(bg, 2);
		assertEquals("select BadGirl_.ID, BadGirl_.NAME, BadGirl_.SPECIALITY from GIRL BadGirl_ where BadGirl_.ID=:p0",
				b.generateQuery());
		checkBadGirl4(b);
	}

	@Test
	void testCount() {

		Girl g = new Girl(1, "sanja", "c++");
		DbSqlBuilder b = dbOom.entities().count(g);
		assertEquals("select count(*) from GIRL Girl_ where (Girl_.ID=:girl.id and Girl_.NAME=:girl.name and Girl_.SPECIALITY=:girl.speciality)",
				b.generateQuery());
		checkGirl(b);

		BadGirl bg = new BadGirl();
		b = dbOom.entities().count(bg);
		assertEquals("select count(*) from GIRL BadGirl_ where (1=1)",
				b.generateQuery());

		bg = new BadGirl(Integer.valueOf(2), null, ".net");
		b = dbOom.entities().count(bg);
		assertEquals("select count(*) from GIRL BadGirl_ where (BadGirl_.ID=:badGirl.fooid and BadGirl_.SPECIALITY=:badGirl.foospeciality)",
				b.generateQuery());
		checkBadGirl1(b);

		b = dbOom.entities().countAll(bg);
		assertEquals("select count(*) from GIRL BadGirl_ where (BadGirl_.ID=:badGirl.fooid and BadGirl_.NAME=:badGirl.fooname and BadGirl_.SPECIALITY=:badGirl.foospeciality)",
				b.generateQuery());
		checkBadGirl2(b);

		b = dbOom.entities().count(BadGirl.class);
		assertEquals("select count(*) from GIRL BadGirl_",
				b.generateQuery());

	}

	@Test
	void testIncreaseDecrease() {
		DbSqlBuilder b = dbOom.entities().increaseColumn(BadBoy.class, 1, "nejm", 5, true);
		assertEquals("update BOY set NAME=NAME+:p0 where BOY.ID=:p1",
				b.generateQuery());

		b = dbOom.entities().increaseColumn(BadBoy.class, 1, "nejm", 5, false);
		assertEquals("update BOY set NAME=NAME-:p0 where BOY.ID=:p1",
				b.generateQuery());

	}

}
