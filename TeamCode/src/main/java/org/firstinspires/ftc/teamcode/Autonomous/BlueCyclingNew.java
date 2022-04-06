package org.firstinspires.ftc.teamcode.Autonomous;

import static org.opencv.core.Core.inRange;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.checkerframework.checker.units.qual.C;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Autonomous.AutoClasses.DirectionCalcClass;
import org.firstinspires.ftc.teamcode.Autonomous.AutoClasses.Odometry;
import org.firstinspires.ftc.teamcode.Autonomous.AutoClasses.SpeedClass;
import org.firstinspires.ftc.teamcode.Autonomous.AutoClasses.TurnControl;
import org.firstinspires.ftc.teamcode.GeneralRobotCode.FreightFrenzyHardwareMap;
import org.firstinspires.ftc.teamcode.TeleOp.Cube_Tracking;
import org.firstinspires.ftc.teamcode.TurretClasses.TurretCombined;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvSwitchableWebcam;

import java.util.ArrayList;
import java.util.List;

@Config
@Autonomous
public class BlueCyclingNew extends LinearOpMode {

    //imports other classes to be used in this program
    FreightFrenzyHardwareMap robot = new FreightFrenzyHardwareMap();
    SpeedClass SpeedClass = new SpeedClass();
    DirectionCalcClass DirectionClass = new DirectionCalcClass();
    TurnControl TurnControl = new TurnControl();
    Odometry OdoClass = new Odometry();
    TurretCombined CombinedTurret = new TurretCombined();
    FtcDashboard dashboard = FtcDashboard.getInstance();
    Telemetry dashboardTelemetry = dashboard.getTelemetry();

    //Declares Varibles
    double breakout; double lastAction = 0;
    double startPointX; double startPointY;
    double timepassed;
    double xSetpoint; double ySetpoint;
    double thetaSetpoint;
    double loopcount;
    double accelerationDistance; double decelerationDistance;
    double slowMoveSpeed;
    double slowMovedDistance;
    double thetaTargetSpeed;
    double thetaDeccelerationDegree;
    double targetSpeed;
    double stopProgram;
    double rotateSetpoint;
    double rotateSpeed;
    double extendSetpoint;
    double extendSpeed;
    double VPivotSetpoint;
    double VPivotSpeed;
    double TSEPos;
    double leftIntakeSet = 0, rightIntakeSet = 0;
    double timeRemaining = 30, startTime;
    double IntakeXSetpoint = 40;
    double YChangingSet = 1;
    double oneLoop = 0;
    boolean hasColorSenssors = false;
    double action2TimeSafe;
    boolean STOPMOTORS = false;
    double rotateIntake = 0;
    double stuckStart = 0, preStuckAction = 0, stuckOne = 0, stuckFixTimer, stuckTiggerOne = 0;
    double stuckOneLoopDelay = 0;
    double waitStart = 0;
    public static double Hmin = 80, Hmax = 180, Smin = 70, Smax = 255, Vmin = 100, Vmax = 255;
    double action;
    boolean IsCameraOpened = false;
    double cubeDifference = 0, cubeChange = 0, CubeTrakingP = .05;

    // Define Webcams

    static OpenCV_Pipeline pipeline;

    WebcamName RightCam;
    WebcamName TurretCam1;
    OpenCvSwitchableWebcam SwitchableWebcam;
    @Override

    public void runOpMode() {
        //declaring FTC Dashboard
        FtcDashboard dashboard = FtcDashboard.getInstance();
        telemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());

        //inits the robot hardwaremap
        robot.init(hardwareMap);

        //Setting the camera names to hardware on the robot
        RightCam = hardwareMap.get(WebcamName.class, "RightCam");
        TurretCam1 = hardwareMap.get(WebcamName.class, "TurretCam1");


        //sets up the camera for openCV piplines
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        SwitchableWebcam = OpenCvCameraFactory.getInstance().createSwitchableWebcam(cameraMonitorViewId, RightCam, TurretCam1);



        //allows to call pipline
        pipeline = new OpenCV_Pipeline();

        //sets the webcam to the pipeline
        SwitchableWebcam.setPipeline(pipeline);
        SwitchableWebcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            //starts the webcam and defines the pixels
            public void onOpened() {

                //sets the active camera for TSE detection
                SwitchableWebcam.setActiveCamera(RightCam);

                SwitchableWebcam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
                //gives FTC dashboard acess to the camera
                FtcDashboard.getInstance().startCameraStream(SwitchableWebcam, 10);
                telemetry.addData("TSECameraOpened", "");
                telemetry.update();
                IsCameraOpened = true;



            }

            //if the camera errors this happens
            @Override
            public void onError(int errorCode) {
                telemetry.addData("TSEcameraNotOpened", 000000000000000000000000000000000000000000000000000000);
                telemetry.update();
                /*
                 * This will be called if the camera could not be opened
`               */
            }
        });



        //this homes the turret and puts the turret in a position to fit in the 18in cube
       while (!opModeIsActive()) {


               VPivotSetpoint = 900;
               VPivotSpeed = 10;
               if(Math.abs(-40 - CombinedTurret.extendModifiedEncoder) < 50 && Math.abs(442 - CombinedTurret.rotateModifiedEncoder) < 50){
                   VPivotSetpoint = 602;

               }else if(VPivotSetpoint > 850){
                   extendSetpoint = -50;
                   extendSpeed = 15;
                   rotateSetpoint = 465;
                   rotateSpeed = 1000;

               }

               CombinedTurret.TurretCombinedMethod(extendSetpoint,extendSpeed,rotateSetpoint,rotateSpeed, VPivotSetpoint,VPivotSpeed, robot.TE_M.getCurrentPosition(), robot.TE_G.getState(), robot.TR_M.getCurrentPosition(), robot.TR_G.getState(), robot.TP_M.getCurrentPosition(), robot.TP_G.getState());
               robot.TR_M.setPower(CombinedTurret.rotateFinalMotorPower);
               robot.TE_M.setPower(CombinedTurret.extendFinalMotorPower);
               robot.TP_M.setPower(CombinedTurret.vPivotFinalMotorPower);
           }


            dashboardTelemetry.update();




        waitForStart();
        //resets encoders to ensure an accurate autonomous
        robot.LF_M.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.LF_M.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        robot.LB_M.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.LB_M.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        robot.RF_M.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.RF_M.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //Sets our intial varible setpoints
        action = 1;
        startTime = getRuntime();
        startPointX = 0;
        startPointY = 0;
        stopProgram = 0;
        xSetpoint = 0;

        ySetpoint = 0;
        thetaSetpoint = 0;
        targetSpeed = .25;
        accelerationDistance = 0;
        decelerationDistance = 1;
        slowMoveSpeed = .25;
        slowMovedDistance = 1;
        thetaDeccelerationDegree = 1;
        thetaTargetSpeed = 4.5;

        loopcount = 0;
        timepassed = 0;
        rotateSpeed = 2300;
        extendSpeed = 35;
        VPivotSpeed = 12;
        TSEPos = 1;
        //main loop for the autonomous code
        while (opModeIsActive() && stopProgram == 0) {
            action = 1;

            //Used for checking if this is the first loop cycle that that action is happening
            lastAction = action;
            if(action == 1) {//dropping in correct level
                if (TSEPos == 3) {//TOP GOAL

                    //holding our drivetrain location while placing preloaded freight
                    if (DirectionClass.distanceFromReturn() <= .7) {
                        StopMotors();
                    }
                    //setting turret positions to drop the preloaded freight
                    rotateSetpoint = 1530;
                    extendSetpoint = 0;
                    VPivotSetpoint = 1620;
                    //making sure the turret moves in a path that does not hit anything
                    if ((CombinedTurret.vPivotModifiedEncoder >= 875)) {
                        extendSetpoint = 865;
                        //once we are in a good position to drop we start a timer and drop the freight
                        if (CombinedTurret.extendModifiedEncoder <= 900 && CombinedTurret.extendModifiedEncoder >= 820) {
                            if (loopcount == 0) {
                                loopcount = 1;
                                timepassed = getRuntime() + 4;
                            }
                            leftIntakeSet = -0.4;
                            rightIntakeSet = 0.4;
                            //after the freight is dropped or the timer is out we move to the next action
                            if (robot.I_DS.getDistance(DistanceUnit.INCH) >= 1 || getRuntime() > timepassed) {
                                StopMotors();
                                //action = 2;
                                startPointY = OdoClass.odoYReturn();
                                startPointX = OdoClass.odoXReturn();
                                leftIntakeSet = 0;
                                rightIntakeSet = 0;
                                breakout = 0;
                            }
                        }
                    }
                }else if (TSEPos == 2) {//MID GOAL
                    //holding our drivetrain location while placing preloaded freight
                    if (DirectionClass.distanceFromReturn() <= .7) {
                        StopMotors();
                    }
                    //setting turret positions to drop the preloaded freight
                    rotateSetpoint = 1535;
                    extendSetpoint = 0;
                    VPivotSetpoint = 1250;
                    //making sure the turret moves in a path that does not hit anything
                    if (CombinedTurret.vPivotModifiedEncoder >= 875) {
                        extendSetpoint = 865;
                        //once we are in a good position to drop we start a timer and drop the freight
                        if (CombinedTurret.extendModifiedEncoder <= 900 && CombinedTurret.extendModifiedEncoder >= 820) {
                            if(loopcount == 0){
                                loopcount = 1;
                                timepassed = getRuntime() + 4;
                            }
                            leftIntakeSet = -.4;
                            rightIntakeSet = .4;
                            //after the freight is dropped or the timer is out we move to the next action
                            if (robot.I_DS.getDistance(DistanceUnit.INCH) >= 1 || getRuntime() > timepassed) {
                                StopMotors();
                                //action = 2;
                                startPointY = OdoClass.odoYReturn();
                                startPointX = OdoClass.odoXReturn();
                                leftIntakeSet = 0;
                                rightIntakeSet = 0;
                                breakout = 0;
                            }
                        }
                    }
                }else if (TSEPos == 1) {// BOTTOM GOAL
                    //holding our drivetrain location while placing preloaded freight
                    if (DirectionClass.distanceFromReturn() <= .7) {
                        STOPMOTORS = true;
                    }
                    //setting turret positions to drop the preloaded freight
                    rotateSetpoint = 1535;
                    extendSetpoint = 0;
                    VPivotSetpoint = 1030;
                    //making sure the turret moves in a path that does not hit anything
                    if (CombinedTurret.vPivotModifiedEncoder >= 800) {
                        extendSetpoint = 800;
                        //once we are in a good position to drop we start a timer and drop the freight
                        if (CombinedTurret.extendModifiedEncoder <= 850 && CombinedTurret.extendModifiedEncoder >= 750) {
                            if(loopcount == 0){
                                loopcount = 1;
                                timepassed = getRuntime() + 4;
                            }
                            leftIntakeSet = -.4;
                            rightIntakeSet = .4;
                            //after the freight is dropped or the timer is out we move to the next action
                            if (robot.I_DS.getDistance(DistanceUnit.INCH) >= 1 || getRuntime() > timepassed) {
                                StopMotors();
                                //action = 2;
                                startPointY = OdoClass.odoYReturn();
                                startPointX = OdoClass.odoXReturn();
                                leftIntakeSet = 0;
                                rightIntakeSet = 0;
                                breakout = 0;
                            }
                        }
                    }
                }
            //a wait function for use if we ever need to put a wait anywhere in the code
            }else if(action == .5){
                if(lastAction != .5){
                    waitStart = getRuntime();
                }
                if(getRuntime() - waitStart > 5){
                    action = 1;
                    startPointX = OdoClass.odoXReturn();
                    startPointY = OdoClass.odoYReturn();
                    breakout = 0;
                    oneLoop = 0;
                    leftIntakeSet = 0;
                    rightIntakeSet = 0;

                }
            }
            //driving to the warehouse line with our front color sensors
            else if(action == 2){
                if(oneLoop == 0){//setting variables only once so we can change them if we need to using our failsafe program
                    //setting drivetrain positions and speeds
                    xSetpoint = 34;
                    ySetpoint = YChangingSet;
                    thetaSetpoint = 0;
                    thetaDeccelerationDegree = 2;
                    thetaTargetSpeed = 4.5;
                    accelerationDistance = 0;
                    decelerationDistance = 7;
                    slowMoveSpeed = 2;
                    slowMovedDistance = 6;
                    targetSpeed = 40;
                    action2TimeSafe = getRuntime();
                    oneLoop = 1;


                }

                //setting the intake position using a safe path to prevent collisions
                extendSetpoint = 230;
                extendSpeed = 40;

                rotateSetpoint = 0;

                //waiting until the extend and rotate are safe to lower the turret
                if(Math.abs(extendSetpoint - CombinedTurret.extendModifiedEncoder) < 100 && Math.abs(rotateSetpoint - CombinedTurret.rotateModifiedEncoder) < 150){
                    VPivotSetpoint = 500;
                    VPivotSpeed = 25;
                }else{
                    VPivotSetpoint = 900;
                    VPivotSpeed = 10;
                }


                if(robot.LF_C.alpha() > 800  || robot.RF_C.alpha() > 800  || OdoClass.odoXReturn() > 35){
                        STOPMOTORS = true;


                    if(CombinedTurret.vPivotModifiedEncoder < 600){
                        action = 3;
                        startPointX = OdoClass.odoXReturn();
                        startPointY = OdoClass.odoYReturn();
                        leftIntakeSet = 0;
                        rightIntakeSet = 0;
                        breakout = 0;
                        oneLoop = 0;
                        STOPMOTORS = false;
                    }

                }



            }
            else if(action == 3){//intaking
                if(oneLoop == 0){//setting variables only once so we can change them if we need to using our failsafe program
                    //setting drivetrain positions and speeds
                    thetaSetpoint = 0;
                    thetaDeccelerationDegree = 2;
                    thetaTargetSpeed = 4.5;
                    accelerationDistance = 0;
                    decelerationDistance = 7;
                    slowMoveSpeed = 8;
                    slowMovedDistance = 6;
                    xSetpoint = 40;
                    ySetpoint = YChangingSet;
                    targetSpeed = 5;
                    leftIntakeSet = .5;
                    rightIntakeSet = -.5;
                    oneLoop = 1;
                }
                if(robot.LB_C.alpha() > 800 || robot.RB_C.alpha() > 800 || OdoClass.odoXReturn() > 39){
                    hasColorSenssors = true;
                }

                if(hasColorSenssors){
                    cubeDifference = 320 - pipeline.cubeCenter;
                    if(Math.abs(cubeDifference) < 10){
                        cubeChange = 0;
                    }else{
                        cubeChange = cubeDifference * CubeTrakingP;
                    }

                    rotateSetpoint = rotateSetpoint - cubeChange;
                    if(pipeline.cubeCenter > 300 && pipeline.cubeCenter < 340 && extendSetpoint < 800){
                        extendSetpoint = extendSetpoint + 50;
                    }else if(extendSetpoint > 800){
                        extendSetpoint = 230;
                        rotateSetpoint = 0;
                        VPivotSetpoint = 500;
                    }
                    robot.RI_S.setPower(-.5);
                    robot.LI_S.setPower(.5);
                }else{
                    rotateSetpoint = 0;
                }



                if(robot.I_DS.getDistance(DistanceUnit.INCH) < 1 && breakout == 1){
                    action = 4;
                    hasColorSenssors = false;
                    oneLoop = 0;
                    IntakeXSetpoint = xSetpoint;
                    StopMotors();
                    startPointX = OdoClass.odoXReturn();
                    startPointY = OdoClass.odoYReturn();
                    breakout = 0;
                    rightIntakeSet = 0;
                   leftIntakeSet = 0;
                }else{
                    breakout = 1;
                }

            }else if(action == 4){//Decision to drop freight or to stop
                timeRemaining = 30 - (getRuntime() - startTime);
                if(timeRemaining > 5){
                    action = 5;
                }else{
                    action = 6;
                }
                startPointX = OdoClass.odoXReturn();
                startPointY = OdoClass.odoYReturn();
                loopcount = 0;
            } else if (action == 5) {//dropping freight in top goal
                thetaSetpoint = 0;
                accelerationDistance = 0;
                decelerationDistance = 20;
                slowMoveSpeed = .5;
                slowMovedDistance = 4;
                thetaDeccelerationDegree = 3;
                thetaTargetSpeed = 4.5;
                xSetpoint = 0;
                ySetpoint = YChangingSet;
                thetaSetpoint = 0;
                targetSpeed = 40;
                if(robot.I_DS.getDistance(DistanceUnit.INCH) > .6){
                    leftIntakeSet = .5;
                    rightIntakeSet = -.5;
                }else{
                    leftIntakeSet = 0;
                    rightIntakeSet = 0;
                }

                if(DirectionClass.distanceFromReturn() < 1){
                    StopMotors();
                }

                VPivotSetpoint = 1485;

                if ((CombinedTurret.vPivotModifiedEncoder >= 875)) {
                    rotateSetpoint = 1440;
                    if(CombinedTurret.rotateModifiedEncoder > 1000){
                        extendSetpoint = 865;
                    }
                    if ((CombinedTurret.extendModifiedEncoder <= 900 && CombinedTurret.extendModifiedEncoder >= 820) && DirectionClass.distanceFromReturn() <= 2) {
                        if (loopcount == 0) {
                            loopcount = 1;
                            timepassed = getRuntime() + 4;
                        }
                        leftIntakeSet = -.4;
                        rightIntakeSet = .4;

                        if (robot.I_DS.getDistance(DistanceUnit.INCH) >= 1 || getRuntime() > timepassed) {
                            StopMotors();
                            action = 2;
                            oneLoop = 0;
                            startPointY = OdoClass.odoYReturn();
                            startPointX = OdoClass.odoXReturn();
                            leftIntakeSet = 0;
                            rightIntakeSet = 0;
                        }
                    }
                }


            } else if (action == 6) {
                extendSetpoint = 200;
                extendSpeed = 20;
                rotateSpeed = 2300;
                rotateSetpoint = 120;
                VPivotSetpoint = 800;
                VPivotSpeed = 8;

                thetaSetpoint = 0;
                accelerationDistance = .25;
                decelerationDistance = 7.5;
                slowMoveSpeed = 1;
                slowMovedDistance = 2;
                thetaDeccelerationDegree = 2;
                thetaTargetSpeed = 4.5;
                xSetpoint = 41;
                ySetpoint = YChangingSet;
                thetaSetpoint = 0;
                targetSpeed = 18;
                if (DirectionClass.distanceFromReturn() <= 1.3 && breakout != 0) {
                    StopMotors();
                    breakout = 0;
                    action = 7;
                    oneLoop = 0;
                    startPointX = OdoClass.odoXReturn();
                    startPointY = OdoClass.odoYReturn();
                } else {
                    breakout = 1;
                }
            }else if(action == 103){
                if(stuckOne == 0){
                    startPointX = OdoClass.odoXReturn();
                    startPointY = OdoClass.odoYReturn();

                    thetaSetpoint = 0;
                    accelerationDistance = .25;
                    decelerationDistance = 7.5;
                    slowMoveSpeed = 1;
                    slowMovedDistance = 2;
                    thetaDeccelerationDegree = 2;
                    thetaTargetSpeed = 4.5;
                    xSetpoint = startPointX - 13;
                    ySetpoint = startPointY - 7;
                    thetaSetpoint = 0;
                    targetSpeed = 10;
                    stuckFixTimer = getRuntime();
                    stuckOne = 1;
                    VPivotSetpoint = 750;
                }
                if(stuckFixTimer + 3 < getRuntime()){
                    action = preStuckAction;
                    oneLoop = 0;
                    stuckOne = 0;
                    stuckStart = getRuntime();
                    YChangingSet = OdoClass.odoYReturn();
                    VPivotSetpoint = 400;
                }

            }else if(action == 105){
                if(stuckOne == 0){
                    startPointX = OdoClass.odoXReturn();
                    startPointY = OdoClass.odoYReturn();

                    thetaSetpoint = 0;
                    accelerationDistance = .25;
                    decelerationDistance = 7.5;
                    slowMoveSpeed = 1;
                    slowMovedDistance = 2;
                    thetaDeccelerationDegree = 2;
                    thetaTargetSpeed = 4.5;
                    xSetpoint = startPointX + 13;
                    ySetpoint = startPointY - 7;
                    thetaSetpoint = 0;
                    targetSpeed = 10;
                    stuckFixTimer = getRuntime();
                    stuckOne = 1;
                    VPivotSetpoint = 750;
                }
                if(stuckFixTimer + 3 < getRuntime()){
                    action = preStuckAction;
                    oneLoop = 0;
                    stuckOne = 0;
                    stuckStart = getRuntime();
                    YChangingSet = OdoClass.odoYReturn();
                    VPivotSetpoint = 400;
                }
            }


            //If nothing else to do, stop the program
            else {
                stopProgram = 1;
                StopMotors();
            }
            if(action == 2 && lastAction != 2){

                YChangingSet = YChangingSet - .55;
                rotateIntake = rotateIntake + 100;
            }
            if(SpeedClass.CurrentSpeed() < 1  && targetSpeed > 5){
                if(stuckTiggerOne == 0){
                    stuckStart = getRuntime();
                    stuckTiggerOne = 1;
                }

            }else{
                stuckStart = getRuntime() + 1;
                stuckTiggerOne = 0;
                stuckTiggerOne = 0;
            }
            if(getRuntime() - stuckStart > 1 && action != 1 && stuckOneLoopDelay == 0){

                if(action < 10){
                    preStuckAction = action;
                }
                if(DirectionClass.LF_M_DirectionReturn() > 0){
                    action = 103;
                }else{
                    action = 105;
                }
            }


            stuckOneLoopDelay = 0;

            //Runs all of our equations each loop cycle
            Movement(xSetpoint, ySetpoint, thetaSetpoint, targetSpeed, thetaTargetSpeed, thetaDeccelerationDegree, slowMoveSpeed, accelerationDistance, decelerationDistance, slowMovedDistance);
            VPivotSetpoint = CombinedTurret.VPivotLimits(extendSetpoint, VPivotSetpoint, rotateSetpoint, false, false);
            CombinedTurret.TurretCombinedMethod(extendSetpoint,extendSpeed,rotateSetpoint,rotateSpeed, VPivotSetpoint,VPivotSpeed, robot.TE_M.getCurrentPosition(), robot.TE_G.getState(), robot.TR_M.getCurrentPosition(), robot.TR_G.getState(), robot.TP_M.getCurrentPosition(), robot.TP_G.getState());

            PowerSetting();
            Telemetry();
        }
        //Cubewebcam.stopStreaming();
        //Cubewebcam.closeCameraDevice();
    }


    public void Telemetry() {
        //Displays telemetry
        telemetry.addData("Action", action);
        telemetry.addData("start Time", startTime);
        telemetry.addData("time left", getRuntime() - startTime);
        telemetry.addData("Odo X", OdoClass.odoXReturn());
        telemetry.addData("Odo Y", OdoClass.odoYReturn());
        telemetry.addData("Theta Angle", OdoClass.thetaInDegreesReturn());
        telemetry.addData("X", DirectionClass.XReturn());
        telemetry.addData("Y", DirectionClass.YReturn());
        telemetry.addData("Distance From", DirectionClass.distanceFromReturn());
        telemetry.addData("Speed", SpeedClass.SpeedReturn());
        telemetry.addData("has color sensors", hasColorSenssors);
        telemetry.addData("current Speed", SpeedClass.CurrentSpeed());
        telemetry.addData("time", getRuntime());
        telemetry.addData("stuck Timer", getRuntime() - stuckStart );
        telemetry.addData("stuck Start", stuckStart);
        telemetry.addData("prestuck action", preStuckAction);
        telemetry.addData("stuck Fix TImer", stuckFixTimer + 3);
        telemetry.addData("leftIntakeSet", leftIntakeSet);
        telemetry.addData("Extend Encoder", CombinedTurret.extendModifiedEncoder);


       // telemetry.addData("PT", robot.TP_P.getVoltage());

        telemetry.addData("Distance Sensor", robot.I_DS.getDistance(DistanceUnit.INCH));
        telemetry.update();
    }

    public void Movement(double endpointx, double endpointy, double thetasetpoint, double targetspeed, double thetaTargetSpeed, double thetaDeccelerationDegree, double slowMoveSpeed, double accelerationdistance, double deccelerationdistance, double slowMovedDistance) {
        OdoClass.RadiusOdometry(robot.LF_M.getCurrentPosition(), robot.LB_M.getCurrentPosition(), robot.RF_M.getCurrentPosition());
        TurnControl.turnControl(thetaSetpoint, OdoClass.thetaInDegreesReturn());
        DirectionClass.DirectionCalc(startPointX, startPointY, endpointx, endpointy, OdoClass.odoXReturn(), OdoClass.odoYReturn(), TurnControl.theta);
        SpeedClass.MotionProfile(targetspeed, accelerationdistance, deccelerationdistance, slowMovedDistance, DirectionClass.distanceReturn(), DirectionClass.distanceFromReturn(), slowMoveSpeed, thetaDeccelerationDegree, thetasetpoint, thetaTargetSpeed, OdoClass.thetaInDegreesReturn());
        SpeedClass.SpeedCalc(OdoClass.odoXReturn(), OdoClass.odoYReturn(), OdoClass.thetaInDegreesReturn(), getRuntime(), SpeedClass.speedSetpoint(), SpeedClass.thetaSpeedSetpoint());
    }

    public void PowerSetting() {
        if(STOPMOTORS == false) {


            robot.TR_M.setPower(CombinedTurret.rotateFinalMotorPower);
            robot.TE_M.setPower(CombinedTurret.extendFinalMotorPower);
            robot.TP_M.setPower(CombinedTurret.vPivotFinalMotorPower);
            robot.LF_M.setPower(DirectionClass.LF_M_DirectionReturn() * (SpeedClass.SpeedReturn()));
            robot.LB_M.setPower(DirectionClass.LB_M_DirectionReturn() * (SpeedClass.SpeedReturn()));
            robot.RF_M.setPower(DirectionClass.RF_M_DirectionReturn() * (SpeedClass.SpeedReturn()));
            robot.RB_M.setPower(DirectionClass.RB_M_DirectionReturn() * (SpeedClass.SpeedReturn()));
            robot.LI_S.setPower(leftIntakeSet);
            robot.RI_S.setPower(rightIntakeSet);
        }else{
            robot.LF_M.setPower(0);
            robot.LB_M.setPower(0);
            robot.RF_M.setPower(0);
            robot.RB_M.setPower(0);
            robot.TR_M.setPower(CombinedTurret.rotateFinalMotorPower);
            robot.TE_M.setPower(CombinedTurret.extendFinalMotorPower);
            robot.TP_M.setPower(CombinedTurret.vPivotFinalMotorPower);
            robot.LI_S.setPower(leftIntakeSet);
            robot.RI_S.setPower(rightIntakeSet);
        }


    }

    public void StopMotors() {
        robot.LF_M.setPower(0);
        robot.LB_M.setPower(0);
        robot.RF_M.setPower(0);
        robot.RB_M.setPower(0);
    }

    //OpenCV pipline for TSE Detection
    public static class OpenCV_Pipeline extends OpenCvPipeline {

        /** Most important section of the code: Colors **/
        static final Scalar GOLD = new Scalar(255, 215, 0);
        static final Scalar CRIMSON = new Scalar(220, 20, 60);
        static final Scalar AQUA = new Scalar(79, 195, 247);
        static final Scalar PARAKEET = new Scalar(3, 192, 74);
        static final Scalar CYAN = new Scalar(0, 139, 139);
        static final Scalar WHITE = new Scalar(255, 255, 255);

        int indexLowest; double yLowest = -10;
        double targetX; double targetY; double targetWidth; double targetArea; double yLeft = -10;
        double cubeCenter = 320;
        double TSELocation = 0;
        boolean TSETrigger = true;
        // Create a Mat object that will hold the color data


        Rect redMask;
        Rect CubeMask;


        List<MatOfPoint> CubeContours;


        List<MatOfPoint> redContours;

        // Make a Constructor
        public OpenCV_Pipeline() {

            redContours = new ArrayList<MatOfPoint>();
            CubeContours = new ArrayList<MatOfPoint>();
        }

        public boolean filterContours(MatOfPoint contour) {
            return Imgproc.contourArea(contour) > 400;
        }

        @Override
        public Mat processFrame(Mat input) {

            Mat YCrCb = new Mat();
            Mat HSV = new Mat();
            Mat RGBA = new Mat();


            if(TSETrigger) {
                Imgproc.cvtColor(input, HSV, Imgproc.COLOR_RGB2HSV);

                Scalar scalarLowerYCrCb = new Scalar(Hmin, Smin, Vmin);
                Scalar scalarUpperYCrCb = new Scalar(Hmax, Smax, Vmax);
                //Scalar scalarLowerYCrCb = new Scalar(15.0, 100.0, 120.0);
                //Scalar scalarUpperYCrCb = new Scalar(45.0, 255.0, 255.0);
                Mat maskRed = new Mat();
                //BLUE DO NOT REMOVE
                //Scalar scalarLowerYCrCb = new Scalar(80.0, 70.0, 100.0);
                //Scalar scalarUpperYCrCb = new Scalar(180.0, 255.0, 255.0);

                //inRange(HSV, lowYellow, highYellow, maskYellow);
                //inRange(HSV, lowWhite, highWhite, maskWhite);
                inRange(HSV, scalarLowerYCrCb, scalarUpperYCrCb, maskRed);


                redContours.clear();


                Imgproc.findContours(maskRed, redContours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.drawContours(input, redContours, -1, GOLD); //input


                yLowest = -640;
                indexLowest = 0;

                if (redContours.size() > 0) {
                    for (int i = 0; i < redContours.size(); i++) {
                        if (filterContours(redContours.get(i))) {
                            redMask = Imgproc.boundingRect(redContours.get(i));
                            Imgproc.rectangle(input, redMask, GOLD, 10);

                            if (redMask.y + redMask.height > 100 && redMask.y + redMask.height < 400) {
                                if (redMask.x + redMask.width > 200 && redMask.x + redMask.width < 300) {
                                    TSELocation = 1;
                                } else if (redMask.x + redMask.width > 300 && redMask.x + redMask.width < 400) {
                                    TSELocation = 2;
                                } else if (redMask.x + redMask.width > 400 && redMask.x + redMask.width < 480) {
                                    TSELocation = 3;
                                }
                            }
                        }
                    }
                    redMask = Imgproc.boundingRect(redContours.get(indexLowest));
                    Imgproc.rectangle(input, redMask, PARAKEET, -5);


                } else {
                    TSELocation = -1;

                }

                maskRed.release();

            }else{//Cube detection
                Imgproc.cvtColor(input, HSV,Imgproc.COLOR_RGB2HSV);

                Scalar scalarLowerYCrCb = new Scalar(Hmin, Smin, Vmin);
                Scalar scalarUpperYCrCb = new Scalar(Hmax, Smax, Vmax);
                //Scalar scalarLowerYCrCb = new Scalar(15.0, 100.0, 120.0);
                //Scalar scalarUpperYCrCb = new Scalar(45.0, 255.0, 255.0);
                Mat maskCube = new Mat();


                inRange(HSV, scalarLowerYCrCb, scalarUpperYCrCb, maskCube);


                CubeContours.clear();


                Imgproc.findContours(maskCube, CubeContours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.drawContours(input, CubeContours, -1, AQUA); //input


                yLowest = -640;
                indexLowest = 0;

                if(CubeContours.size() > 0){
                    for (int i = 0; i < CubeContours.size(); i++){
                        if (filterContours(CubeContours.get(i))){
                            CubeMask = Imgproc.boundingRect(CubeContours.get(i));
                            Imgproc.rectangle(input, CubeMask, AQUA, 2);

                            if(Math.abs((CubeMask.y + CubeMask.height) - yLowest) < 80){
                                if(CubeMask.x + CubeMask.width > yLeft){
                                    indexLowest = i;
                                    yLowest = CubeMask.y + CubeMask.height;
                                    yLeft = CubeMask.x + CubeMask.width;
                                }
                            }else if(CubeMask.y + CubeMask.height > yLowest){
                                indexLowest = i;
                                yLowest = CubeMask.y + CubeMask.height;
                                yLeft = CubeMask.x + CubeMask.width;
                            }
                        }
                    }
                    CubeMask = Imgproc.boundingRect(CubeContours.get(indexLowest));
                    Imgproc.rectangle(input, CubeMask, PARAKEET, -5);
                    targetX = CubeMask.x;
                    targetY = CubeMask.y;
                    targetWidth = CubeMask.width;
                    targetArea = CubeMask.height * CubeMask.width;
                    cubeCenter = targetX + (targetWidth/2);

                }else{
                    targetX = -1;
                    targetY = -1;
                    targetWidth = -1;
                    targetArea = -1;
                    cubeCenter = 320;

                }

                maskCube.release();
            }



            YCrCb.release();
            RGBA.release();
            HSV.release();



            return input;
        }
    }



}


