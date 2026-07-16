package es.upm.grise.cruiseControl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import es.upm.grise.cruiseControl.exceptions.CannotSetSpeedLimitException;
import es.upm.grise.cruiseControl.exceptions.IncorrectSpeedLimitException;
import es.upm.grise.cruiseControl.exceptions.IncorrectSpeedSetException;
import es.upm.grise.cruiseControl.exceptions.SpeedSetAboveSpeedLimitException;

class CruiseControlTest {


	@Test
	public void constructorInitializesSpeedSetAndLimitToNull() {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		assertNull(cc.getSpeedSet());
		assertNull(cc.getSpeedLimit());
		assertFalse(cc.isEnabled());
	}

	

	@Test
	public void setSpeedSetPositiveEnablesControl() throws Exception {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		cc.setSpeedSet(90);
		assertEquals(Integer.valueOf(90), cc.getSpeedSet());
		assertTrue(cc.isEnabled());
	}

	@Test
	public void setSpeedSetZeroThrowsIncorrectSpeedSet() {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		assertThrows(IncorrectSpeedSetException.class, () -> cc.setSpeedSet(0));
	}

	@Test
	public void setSpeedSetNegativeThrowsIncorrectSpeedSet() {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		assertThrows(IncorrectSpeedSetException.class, () -> cc.setSpeedSet(-10));
	}

	@Test
	public void setSpeedSetEqualToLimitIsAllowed() throws Exception {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		cc.setSpeedLimit(100);
		cc.setSpeedSet(100);
		assertEquals(Integer.valueOf(100), cc.getSpeedSet());
	}

	@Test
	public void setSpeedSetBelowLimitIsAllowed() throws Exception {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		cc.setSpeedLimit(100);
		cc.setSpeedSet(80);
		assertEquals(Integer.valueOf(80), cc.getSpeedSet());
	}

	@Test
	public void setSpeedSetAboveLimitThrowsException() throws Exception {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		cc.setSpeedLimit(100);
		assertThrows(SpeedSetAboveSpeedLimitException.class, () -> cc.setSpeedSet(120));
	}

	@Test
	public void setSpeedSetWithoutLimitAcceptsAnyPositiveValue() throws Exception {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		cc.setSpeedSet(500);
		assertEquals(Integer.valueOf(500), cc.getSpeedSet());
	}

	// ---------- setSpeedLimit ----------

	@Test
	public void setSpeedLimitPositiveStoresValue() throws Exception {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		cc.setSpeedLimit(110);
		assertEquals(Integer.valueOf(110), cc.getSpeedLimit());
	}

	@Test
	public void setSpeedLimitZeroThrowsIncorrectSpeedLimit() {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		assertThrows(IncorrectSpeedLimitException.class, () -> cc.setSpeedLimit(0));
	}

	@Test
	public void setSpeedLimitNegativeThrowsIncorrectSpeedLimit() {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		assertThrows(IncorrectSpeedLimitException.class, () -> cc.setSpeedLimit(-5));
	}

	@Test
	public void setSpeedLimitAfterSpeedSetThrowsCannotSetSpeedLimit() throws Exception {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		cc.setSpeedSet(90);
		assertThrows(CannotSetSpeedLimitException.class, () -> cc.setSpeedLimit(110));
	}

	// ---------- disable ----------

	@Test
	public void disableClearsSpeedSetAndDisables() throws Exception {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		cc.setSpeedSet(90);
		cc.disable();
		assertFalse(cc.isEnabled());
		assertNull(cc.getSpeedSet());
	}

	// ---------- nextCommand ----------

	@Test
	public void nextCommandIdleWhenSpeedSetNotInitialized() {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		assertEquals(Command.IDLE, cc.nextCommand().command);
	}

	@Test
	public void nextCommandIdleWhenDisabled() throws Exception {
		CruiseControl cc = new CruiseControl(new RoadInformation(), new Speedometer());
		cc.setSpeedSet(90);
		cc.disable();
		assertEquals(Command.IDLE, cc.nextCommand().command);
	}

	@Test
	public void nextCommandReduceWhenCurrentSpeedAboveSpeedSet() throws Exception {
		// road min 60, current 100, speedSet 80 -> above speedSet, not below min -> REDUCE
		CruiseControl cc = new CruiseControl(new RoadInformation(120, 60), new Speedometer(100));
		cc.setSpeedSet(80);
		assertEquals(Command.REDUCE, cc.nextCommand().command);
	}

	@Test
	public void nextCommandIncreaseWhenAboveSpeedSetButBelowRoadMinSpeed() throws Exception {
		// current 50, road min 60, speedSet 40 -> above speedSet but below road min -> INCREASE
		CruiseControl cc = new CruiseControl(new RoadInformation(120, 60), new Speedometer(50));
		cc.setSpeedSet(40);
		assertEquals(Command.INCREASE, cc.nextCommand().command);
	}

	@Test
	public void nextCommandIncreaseWhenCurrentSpeedBelowSpeedSet() throws Exception {
		// current 80, road max 120, speedSet 100 -> below speedSet, not above max -> INCREASE
		CruiseControl cc = new CruiseControl(new RoadInformation(120, 60), new Speedometer(80));
		cc.setSpeedSet(100);
		assertEquals(Command.INCREASE, cc.nextCommand().command);
	}

	@Test
	public void nextCommandReduceWhenBelowSpeedSetButAboveRoadMaxSpeed() throws Exception {
		// current 130, road max 120, speedSet 150 -> below speedSet but above road max -> REDUCE
		CruiseControl cc = new CruiseControl(new RoadInformation(120, 60), new Speedometer(130));
		cc.setSpeedSet(150);
		assertEquals(Command.REDUCE, cc.nextCommand().command);
	}

	@Test
	public void nextCommandKeepWhenCurrentSpeedEqualsSpeedSet() throws Exception {
		CruiseControl cc = new CruiseControl(new RoadInformation(120, 60), new Speedometer(90));
		cc.setSpeedSet(90);
		assertEquals(Command.KEEP, cc.nextCommand().command);
	}

}