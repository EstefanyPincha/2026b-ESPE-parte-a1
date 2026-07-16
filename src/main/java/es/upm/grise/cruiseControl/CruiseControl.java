package es.upm.grise.cruiseControl;

import es.upm.grise.cruiseControl.exceptions.CannotSetSpeedLimitException;
import es.upm.grise.cruiseControl.exceptions.IncorrectSpeedLimitException;
import es.upm.grise.cruiseControl.exceptions.IncorrectSpeedSetException;
import es.upm.grise.cruiseControl.exceptions.SpeedSetAboveSpeedLimitException;

public class CruiseControl {

	private RoadInformation roadInformation;
	private Speedometer speedometer;
	private Integer speedLimit;
	private Integer speedSet;
	private boolean enabled = false;

	public CruiseControl(RoadInformation roadInformation, Speedometer speedometer) {
		this.roadInformation = roadInformation;
		this.speedometer = speedometer;
		this.speedLimit = null;
		this.speedSet = null;
	}

	public void setSpeedSet(int speedSet) throws IncorrectSpeedSetException, SpeedSetAboveSpeedLimitException {

		if(speedSet <= 0)
			throw new IncorrectSpeedSetException();

		if(speedLimit != null && speedSet > speedLimit)
			throw new SpeedSetAboveSpeedLimitException();

		this.speedSet = speedSet;
		this.enabled = true;
	}

	public void setSpeedLimit(int speedLimit) throws IncorrectSpeedLimitException, CannotSetSpeedLimitException {

		if(speedLimit <= 0)
			throw new IncorrectSpeedLimitException();

		if(speedSet != null)
			throw new CannotSetSpeedLimitException();

		this.speedLimit = speedLimit;
	}

	public void disable() {
		this.enabled = false;
		this.speedSet = null;
	}

	public Response nextCommand() {

		Response response = new Response();

		if(speedSet == null || !enabled) {
			response.command = Command.IDLE;
			return response;
		}

		int currentSpeed = speedometer.getCurrentSpeed();
		int maxRoadSpeed = roadInformation.getMaxSpeed();
		int minRoadSpeed = roadInformation.getMinSpeed();

		if(currentSpeed > speedSet) {
			if(currentSpeed < minRoadSpeed)
				response.command = Command.INCREASE;
			else
				response.command = Command.REDUCE;
		} else if(currentSpeed < speedSet) {
			if(currentSpeed > maxRoadSpeed)
				response.command = Command.REDUCE;
			else
				response.command = Command.INCREASE;
		} else {
			response.command = Command.KEEP;
		}

		return response;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Integer getSpeedLimit() {
		return speedLimit;
	}

	public Integer getSpeedSet() {
		return speedSet;
	}

}