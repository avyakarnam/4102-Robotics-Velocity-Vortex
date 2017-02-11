package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.utilities.Hardware;
import org.firstinspires.ftc.teamcode.components.Intake;
import org.firstinspires.ftc.teamcode.components.Lift;
import org.firstinspires.ftc.teamcode.components.Shooter;
import org.firstinspires.ftc.teamcode.utilities.Utils;
import org.firstinspires.ftc.teamcode.components.Wheels;

/**
 * Created by benorgera on 10/24/16.
 */
@TeleOp(name = "Shmoney Yerd L17", group = "4102")
public class DriverControlled extends LinearOpMode {

    //modes
    private boolean isSlowMode = false;
    private boolean intakeIsFront = false;

    //components
    private Lift lift;
    private Shooter shooter;
    private Wheels wheels;
    private Intake intake;

    private double shotPower = 6;
    
    private boolean wasTogglingDirection = false;
    private boolean wasTogglingSlowMode = false;
    private boolean wasUppingShotPower = false;
    private boolean wasDowningShotPower = false;
    private boolean wasShooting = false;
    private boolean wasTogglingIntake = false;

    private boolean hasDroppedFork = false;

    private long startTime;

    private final double slowModeConstant = 0.3;

    @Override
    public void runOpMode() {
        Hardware.init(hardwareMap, this, false, telemetry);

        lift = Hardware.getLift();
        wheels = Hardware.getWheels();
        intake = Hardware.getIntake();
        shooter = Hardware.getShooter();

        telemetry.addData("4102", "Let's kick up");
        telemetry.update();

        waitForStart(); //wait for the match to start

        startTime = System.currentTimeMillis(); //store the start time, so we can print remaining match time

        while (Hardware.active()) run(); //control to robot using gamepad input while the opMode is active

        Hardware.freezeAllMotorFunctions(); //stop all motors in case any are running

        telemetry.addData("4102", "We kicked up");
        telemetry.update();
    }

    private void run() { //control to robot using gamepad input

        //--------------------------DRIVING-------------------------------

        wheels.drive((isSlowMode ? slowModeConstant : 1) * (intakeIsFront ? 1 : -1) * gamepad1.left_stick_x, (isSlowMode ? slowModeConstant : 1) * (intakeIsFront ? -1 : 1) * gamepad1.left_stick_y, (isSlowMode ? slowModeConstant : 1) * gamepad1.right_stick_x, !isSlowMode);

        if (gamepad1.a && !wasTogglingDirection)
            intakeIsFront = !intakeIsFront;

        wasTogglingDirection = gamepad1.a;

        if (gamepad1.b && !wasTogglingSlowMode)
            isSlowMode =! isSlowMode;

        wasTogglingSlowMode = gamepad1.b;


        //--------------------------SHOOTER-------------------------------

        if (gamepad2.dpad_right && !wasUppingShotPower)
            shotPower = Utils.trim(0, 10, shotPower + 0.5);

        wasUppingShotPower = gamepad2.dpad_right;

        if (gamepad2.dpad_left && !wasDowningShotPower)
            shotPower = Utils.trim(0, 10, shotPower - 0.5);

        wasDowningShotPower = gamepad2.dpad_left;

        if (gamepad2.b && !intake.isRunning() && !wasShooting)
            shooter.shoot(shotPower);

        wasShooting = gamepad2.b;




        //--------------------------Intake-------------------------------

        if (gamepad2.a && !wasTogglingIntake)
            if (intake.isRunning())
                intake.stopIntaking();
            else
                intake.startIntaking(!gamepad2.left_bumper);

        wasTogglingIntake = gamepad2.a;



        //--------------------------LIFT-------------------------------

        //drop fork if it hasn't been dropped before
        if (!hasDroppedFork && (hasDroppedFork = gamepad2.x))
            lift.dropFork();

        if (gamepad2.dpad_up && hasDroppedFork) //raise the lift if we've dropped the fork
            lift.raise();
        else if (gamepad2.dpad_down && hasDroppedFork) //lower the lift if we've dropped the fork
            lift.lower();
        else
            lift.stop();

        telemetry.addData("SHOT", Math.round(shotPower * 10) / 10);
        telemetry.addData("MODE", intakeIsFront ? "INTAKE" : "SHOOT");
        if (isSlowMode) telemetry.addData("SLOW MODE", "TRUE");
        telemetry.addData("TIME", getTimeString());
        telemetry.update();
    }

    private String getTimeString() { //get remaining match time as a string
        int deltaSeconds = 120 - (int) (System.currentTimeMillis() - startTime) / 1000,
                deltaMin = deltaSeconds / 60,
                seconds = deltaSeconds % 60;

        return "" + deltaMin + ":" + (("" + seconds).length() == 1 ? "0" + seconds : seconds) + (seconds <= 30 && deltaMin == 0 ? "  END GAME" : "");
    }
}
