package org.firstinspires.ftc.robotcontroller.internal.components;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcontroller.internal.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by benorgera on 10/24/16.
 */
public class Wheels {
    private DcMotor[][] wheelBase;

    //constants
    private final double stickThreshold = 0.1;
    private final double[][] compensationConstants = {
            {1, 1},
            {1, 1}
    };

    public Wheels(DcMotor frontLeft, DcMotor frontRight, DcMotor backLeft, DcMotor backRight) {

        this.wheelBase = new DcMotor[][] { //initialize motors
                {frontLeft, frontRight},
                {backLeft, backRight}
        };

        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++) { //set up motors
            wheelBase[i][j].setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            wheelBase[i][j].setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }

    public String drive(double xVel, double yVel, double angularVel, boolean isChannelMode) { //translate the robot given velocity and angular velocity, and return a string representation of this algorithm

        if (isChannelMode) { //in channel mode negligible velocities will be disregarded
            xVel = Math.abs(xVel) < stickThreshold ? 0 : xVel;
            yVel = Math.abs(yVel) < stickThreshold ? 0 : yVel;
            angularVel = Math.abs(angularVel) < stickThreshold ? 0 : angularVel;
        }

        double robotVel = Utils.trim(0, 1, Utils.getMagnitude(xVel, yVel)), //magnitude of robot velocity [0, 1]
                theta = Utils.atan3(yVel, xVel); //robot translation angle [0, 2π]

        double[][] relativeWheelVelocities = scaleToUnitVectors(new double[][] {{
                Math.sin(theta - Math.PI / 4), //front left and back right wheel relative velocity [-1, 1] (without angular velocity)
                Math.cos(theta - Math.PI / 4)
        }});

        for (int i = 0; i < 2; i++) relativeWheelVelocities[0][i] *= robotVel;

        return "vel: " + Utils.toString(robotVel) +
                ", theta: " + Utils.toString(theta) +
                ", ngVel: " + Utils.toString(angularVel) +
                ", mode: " + (isChannelMode ? "chan" : "prec") +
                ", pow: " + setWheelPowers(scaleToUnitVectors(new double[][] {
                    {
                        compensationConstants[0][0] * (relativeWheelVelocities[0][0] + angularVel), //front left wheel relative velocity (with angular velocity)
                        compensationConstants[0][1] * (relativeWheelVelocities[0][1] - angularVel) //front right wheel relative velocity (with angular velocity)
                    }, {
                        compensationConstants[1][0] * (relativeWheelVelocities[0][1] + angularVel), //back left wheel relative velocity (with angular velocity)
                        compensationConstants[1][1] * (relativeWheelVelocities[0][0] - angularVel) //back right wheel relative velocity (with angular velocity)
                    }
        }));
    }


    private String setWheelPowers(double[][] wheelPowers) { //apply power to the motors, and return string representation of the wheel powers
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++)
                wheelBase[i][j].setPower(wheelPowers[i][j]);

        return "{ [" + Utils.toString(wheelPowers[0][0]) + ", " + Utils.toString(wheelPowers[0][1]) + "], [" + Utils.toString(wheelPowers[1][0]) + ", " + Utils.toString(wheelPowers[1][1]) + "] }";
    }

    public void stop() { //stop the robot
        for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++)
            wheelBase[i][j].setPower(0);
    }

    private double[][] scaleToUnitVectors(double[][] wheelPowers) { //scale wheel powers so robot can use maximum powers with any combination of rotation and translation

        double max = Utils.getMaxMagnitude(wheelPowers); //maximum magnitude of the relative wheel velocities

        for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++) //convert the relative velocities into wheel voltage multipliers with a maximum magnitude of the robot velocity
            wheelPowers[i][j] /= max;

        return wheelPowers;
    }

}
