package org.firstinspires.ftc.teamcode.GeneralRobotCode.OpenCVTestFolder;

import static org.opencv.core.Core.compare;
import static org.opencv.core.Core.inRange;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.checkerframework.checker.units.qual.A;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;

import org.opencv.videoio.Videoio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Autonomous
public class OpenCV_Masking_Original extends LinearOpMode {

    // Define Webcam
    OpenCvCamera webcam;

    // Create Pipeline
    static OpenCV_Pipeline pipeline;

    @Override
    public void runOpMode() {

        // Set up webcam
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "LeftCam"), cameraMonitorViewId);

        // Set up pipeline
        pipeline = new OpenCV_Pipeline();
        webcam.setPipeline(pipeline);

        // Start camera streaming
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
                FtcDashboard dashboard = FtcDashboard.getInstance();
                telemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
                FtcDashboard.getInstance().startCameraStream(webcam, 10);
            }

            @Override
            public void onError(int errorCode)
            {
                /*
                 * This will be called if the camera could not be opened
                 */
            }
        });

        telemetry.addLine("Waiting for start");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()){
            // Telemetry readings for the HSV values in each region
//            telemetry.addData("Region 1", "%7d, %7d, %7d", pipeline.RGB_Value[0], pipeline.RGB_Value[1], pipeline.RGB_Value[2]);

            telemetry.update();
        }
    }


    public static class OpenCV_Pipeline extends OpenCvPipeline {

        /** Most important section of the code: Colors **/
        static final Scalar GOLD = new Scalar(255, 215, 0);
        static final Scalar CRIMSON = new Scalar(220, 20, 60);
        static final Scalar AQUA = new Scalar(79, 195, 247);
        static final Scalar PARAKEET = new Scalar(3, 192, 74);
        static final Scalar CYAN = new Scalar(0, 139, 139);
        static final Scalar WHITE = new Scalar(255, 255, 255);

        // Create a Mat object that will hold the color data
        
        Rect yellowMask;
        Rect whiteMask;
        Rect redMask;

        List<MatOfPoint> yellowContours;
        List<MatOfPoint> whiteContours;
        List<MatOfPoint> redContours;

        // Make a Constructor
        public OpenCV_Pipeline() {
            yellowContours = new ArrayList<MatOfPoint>();
            whiteContours = new ArrayList<MatOfPoint>();
            redContours = new ArrayList<MatOfPoint>();
        }

        public boolean filterContours(MatOfPoint contour) {
            return Imgproc.contourArea(contour) > 150;
        }

        @Override
        public Mat processFrame(Mat input) {

            Mat YCrCb = new Mat(input.rows(), input.cols(), input.type());

            Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);

            Scalar scalarLowerYCrCb = new Scalar(0.0, 150.0, 120.0);
            Scalar scalarUpperYCrCb = new Scalar(255.0, 255.0, 255.0);
            Mat maskRed = new Mat();



            inRange(YCrCb, scalarLowerYCrCb, scalarUpperYCrCb, maskRed);


//            Core.inRange();

            yellowContours.clear();
            whiteContours.clear();
            redContours.clear();

         /*   Imgproc.findContours(maskYellow, yellowContours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            Imgproc.drawContours(input, yellowContours, -1, CRIMSON); //input

            Imgproc.findContours(maskWhite, whiteContours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            Imgproc.drawContours(input, whiteContours, -1, AQUA); //input*/

            Imgproc.findContours(maskRed, redContours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            Imgproc.drawContours(input, redContours, -1, AQUA);

            for (int i = 0; i < yellowContours.size(); i++){
                if (filterContours(yellowContours.get(i))){
                    yellowMask = Imgproc.boundingRect(yellowContours.get(i));
                    Imgproc.rectangle(input, yellowMask, CRIMSON, 2);
                }
            }

            for (int i = 0; i < redContours.size(); i++){
                if (filterContours(redContours.get(i))){
                    redMask = Imgproc.boundingRect(redContours.get(i));
                    Imgproc.rectangle(input, redMask, CRIMSON, 2);
                }
            }

           /* for (int i = 0; i < whiteContours.size(); i++){
                if (filterContours(whiteContours.get(i))){
                    whiteMask = Imgproc.boundingRect(whiteContours.get(i));
                    Imgproc.rectangle(input, whiteMask, AQUA, 2);
                }
            }*/
            //yellowMask = Imgproc.boundingRect(maskYellow);
            //Imgproc.rectangle(input, yellowMask, GOLD, 2); // input
            //whiteMask = Imgproc.boundingRect(maskWhite);
            //Imgproc.rectangle(input, whiteMask, PARAKEET, 2); // input

            maskRed.release();
            YCrCb.release();
            return input;
        }
    }
}