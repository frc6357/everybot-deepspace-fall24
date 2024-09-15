// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

// Imports that allow the usage of REV Spark Max motor controllers
import com.revrobotics.CANSparkBase;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Robot extends TimedRobot {
  /*
   * Autonomous selection options.
   */

  enum AutoAction {
    AUTO_NOP,
    AUTO_DEPLOY_LVL1,
    AUTO_DEPLOY_LVL2,
    AUTO_LRKT_PANEL_NEAR,
    AUTO_LRKT_PANEL_FAR,
    AUTO_LRKT_CARGO_LVL1,
    AUTO_LCSHP_PANEL_POS1,
    AUTO_LCSHP_PANEL_POS2,
    AUTO_LCSHP_PANEL_POS3,
    AUTO_LCSHP_PANEL_POS4,
    AUTO_LCSHP_CARGO_POS1,
    AUTO_LCSHP_CARGO_POS2,
    AUTO_LCSHP_CARGO_POS3,
    AUTO_LCSHP_CARGO_POS4,
    AUTO_RRKT_PANEL_NEAR,
    AUTO_RRKT_PANEL_FAR,
    AUTO_RRKT_CARGO_LVL1,
    AUTO_RCSHP_PANEL_POS1,
    AUTO_RCSHP_PANEL_POS2,
    AUTO_RCSHP_PANEL_POS3,
    AUTO_RCSHP_PANEL_POS4,
    AUTO_RCSHP_CARGO_POS1,
    AUTO_RCSHP_CARGO_POS2,
    AUTO_RCSHP_CARGO_POS3,
    AUTO_RCSHP_CARGO_POS4,
  }
  private final SendableChooser<AutoAction> m_chooser = new SendableChooser<>();

  /*
   * Drive motor controller instances.
   *
   * Change the id's to match your robot.
   * Change kBrushed to kBrushless if you are using NEOs.
   * The rookie kit comes with CIMs which are brushed motors.
   * Use the appropriate other class if you are using different controllers.
   */
  CANSparkBase leftRear = new CANSparkMax(1, MotorType.kBrushless);
  CANSparkBase leftFront = new CANSparkMax(2, MotorType.kBrushless);
  CANSparkBase rightRear = new CANSparkMax(3, MotorType.kBrushless);
  CANSparkBase rightFront = new CANSparkMax(4, MotorType.kBrushless);

  /*
   * A class provided to control your drivetrain. Different drive styles can be passed to differential drive:
   * https://github.com/wpilibsuite/allwpilib/blob/main/wpilibj/src/main/java/edu/wpi/first/wpilibj/drive/DifferentialDrive.java
   */
  DifferentialDrive m_drivetrain;

  /*
   FIXME: additional objects for mechanism controls
   1 - Cargo belt motor drive belt: brushless (NEO) using SparkMax
   1 - Hatch motor (window motor): need details
   */

  /*
   * Cargo motor controller instances.
   *
   * Like the drive motors, set the CAN id's to match your robot or use different
   * motor controller classes (CANSparkMax) to match your robot as necessary.
   */
  CANSparkBase m_cargoBelt = new CANSparkMax(5, MotorType.kBrushless);

  /*
   * Hatch grabber motor controller instance.
   *
   * Like the drive motors, set the CAN id's to match your robot or use different
   * motor controller classes (CANSparkMax) to match your robot as necessary.
   */
  CANSparkBase m_hatchGrabber = new CANSparkMax(6, MotorType.kBrushed);

    /**
   * The starter code uses the most generic joystick class.
   *
   * To determine which button on your controller corresponds to which number, open the FRC
   * driver station, go to the USB tab, plug in a controller and see which button lights up
   * when pressed down
   *
   * Buttons index from 0
   */
  Joystick m_driverController = new Joystick(0);
  Joystick m_manipController = new Joystick(1);


  // --------------- Magic numbers. Use these to adjust settings. ---------------

 /**
   * How many amps can an individual drivetrain motor use.
   */
  static final int DRIVE_CURRENT_LIMIT_A = 60;

  /**
   * How many amps the cargo belt can use.
   */
  static final int CARGO_BELT_CURRENT_LIMIT_A = 30;

  /**
    * How many amps the hatch grabber motor can use.
    */
  static final int HATCH_GRABBER_CURRENT_LIMIT_A = 5;

  /**
   * Percent output to run the cargo belt when expelling cargo
   */
  static final double CARGO_BELT_CSHIP_SPEED = 1.0;
  static final double CARGO_BELT_RKT_SPEED = 1.0;

  /**
   * Percent output to run the cargo belt when grabbing cargo
   */
  static final double CARGO_BELT_IN_SPEED = -.4;


  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("do nothing", AutoAction.AUTO_NOP);
    m_chooser.addOption("Deploy from Level 1", AutoAction.AUTO_DEPLOY_LVL1);
    m_chooser.addOption("Deploy from Level 2", AutoAction.AUTO_DEPLOY_LVL2);
    m_chooser.addOption("Deliver cargo to cargo ship, left", AutoAction.AUTO_LCSHP_CARGO_POS1);
    m_chooser.addOption("Deliver cargo to cargo ship, right", AutoAction.AUTO_RCSHP_CARGO_POS1);
    SmartDashboard.putData("Auto choices", m_chooser);



    /*
     * Apply the current limit to the drivetrain motors
     */
    leftRear.setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);
    leftFront.setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);
    rightRear.setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);
    rightFront.setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);

    /*
     * Tells the rear wheels to follow the same commands as the front wheels
     */
    leftRear.follow(leftFront);
    rightRear.follow(rightFront);

    /*
     * One side of the drivetrain must be inverted, as the motors are facing opposite directions
     */
    leftFront.setInverted(true);
    rightFront.setInverted(false);

    m_drivetrain = new DifferentialDrive(leftFront, rightFront);

    /*
     * Cargo belt spinning the wrong direction? Change to true here.
     *
     * Add white tape to wheel to help determine spin direction.
     */
    m_cargoBelt.setInverted(true);

    /*
     * Apply the current limit to the cargo mechanism
     */
    m_cargoBelt.setSmartCurrentLimit(CARGO_BELT_CURRENT_LIMIT_A);

    /*
     * Cargo belt spinning the wrong direction? Change to true here.
     *
     * Add white tape to wheel to help determine spin direction.
     */
    m_hatchGrabber.setInverted(false);

    /*
     * Apply the current limit to the cargo mechanism
     */
    m_hatchGrabber.setSmartCurrentLimit(HATCH_GRABBER_CURRENT_LIMIT_A);
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test modes.
   */
  @Override
  public void robotPeriodic() {
    SmartDashboard.putNumber("Time (seconds)", Timer.getFPGATimestamp());
  }


  /*
   * Auto constants, change values below in autonomousInit()for different autonomous behaviour
   *
   * A delayed action starts X seconds into the autonomous period
   *
   * A time action will perform an action for X amount of seconds
   *
   * Speeds can be changed as desired and will be set to 0 when
   * performing an auto that does not require the system
   */
  double AUTO_CARGO_DELAY_S;
  double AUTO_DRIVE_DELAY_S;

  double AUTO_DRIVE_TIME_S;

  double AUTO_DRIVE_SPEED;
  double AUTO_CARGO_BELT_SPEED;

  double autonomousStartTime;

  @Override
  public void autonomousInit() {
    leftRear.setIdleMode(IdleMode.kBrake);
    leftFront.setIdleMode(IdleMode.kBrake);
    rightRear.setIdleMode(IdleMode.kBrake);
    rightFront.setIdleMode(IdleMode.kBrake);

    AUTO_CARGO_DELAY_S = 2;
    AUTO_DRIVE_DELAY_S = 3;

    AUTO_DRIVE_TIME_S = 2.0;
    AUTO_DRIVE_SPEED = -0.5;
    AUTO_CARGO_BELT_SPEED = CARGO_BELT_CSHIP_SPEED;

    /*
     * Depending on which auto is selected, speeds for the unwanted subsystems are set to 0
     * if they are not used for the selected auto
     *
     * For deploy, you can also change the drive delay
     */
    switch(m_chooser.getSelected()) {
      case AUTO_NOP:
        AUTO_DRIVE_SPEED = 0;
        AUTO_CARGO_BELT_SPEED = 0;
        break;
      case AUTO_DEPLOY_LVL1:
        AUTO_CARGO_BELT_SPEED = 0;
        break;
      case AUTO_DEPLOY_LVL2:
        AUTO_DRIVE_SPEED *= 0.8;  // slow down when coming from level2
        AUTO_DRIVE_TIME_S += 3;   // additional drive time from level2
        AUTO_CARGO_BELT_SPEED = 0;
        break;
      case AUTO_LCSHP_CARGO_POS1:
      case AUTO_RCSHP_CARGO_POS1:
        // leave drive and expected belt speed at defaults
        break;
      // Additional options for cargo/hatch scoring positions
      default:
        // Not supported
        AUTO_DRIVE_SPEED = 0;
        AUTO_CARGO_BELT_SPEED = 0;
        break;
    }

    autonomousStartTime = Timer.getFPGATimestamp();
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {

    double timeElapsed = Timer.getFPGATimestamp() - autonomousStartTime;

    /*
     * Drives until time is greater than AUTO_DRIVE_TIME_S
     *
     * Wait time is greater than AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S for complete stop
     *
     * Spins up cargo belt until time spent in auto is greater than AUTO_CARGO_DELAY_S, plus the stop time
     *
     * Stops cargo belt when time is greater than AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S + AUTO_CARGO_DELAY_S
     */
    if(timeElapsed < AUTO_DRIVE_TIME_S)
    {
      m_drivetrain.arcadeDrive(AUTO_DRIVE_SPEED, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S)
    {
      m_cargoBelt.set(0);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if (timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S + AUTO_CARGO_DELAY_S) {
      m_drivetrain.arcadeDrive(0, 0);
      m_cargoBelt.set(AUTO_CARGO_BELT_SPEED);
    }
    else
    {
      m_cargoBelt.set(0);
      m_drivetrain.arcadeDrive(0, 0);
    }
    /* For an explanation on differential drive, squaredInputs, arcade drive and tank drive see the bottom of this file */
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    /*
     * Motors can be set to idle in brake or coast mode.
     *
     * Brake mode effectively shorts the leads of the motor when not running, making it more
     * difficult to turn when not running.
     *
     * Coast doesn't apply any brake and allows the motor to spin down naturally with the robot's momentum.
     *
     * (touch the leads of a motor together and then spin the shaft with your fingers to feel the difference)
     *
     * This setting is driver preference. Try setting the idle modes below to kBrake to see the difference.
     */
    leftRear.setIdleMode(IdleMode.kCoast);
    leftFront.setIdleMode(IdleMode.kCoast);
    rightRear.setIdleMode(IdleMode.kCoast);
    rightFront.setIdleMode(IdleMode.kCoast);
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    /*
     * Spins cargo belt to place in cargo ship
     */
    if (m_manipController.getRawButton(6))
    {
      m_cargoBelt.set(CARGO_BELT_CSHIP_SPEED);
    }
    else if(m_manipController.getRawButtonReleased(6))
    {
      m_cargoBelt.set(0);
    }

    /*
     * While the button is being held spin both cargo belt motor to intake cargo
     */
    if(m_manipController.getRawButton(5))
    {
      m_cargoBelt.set(CARGO_BELT_IN_SPEED);
    }
    else if(m_manipController.getRawButtonReleased(5))
    {
      m_cargoBelt.set(0);
    }

    /*
     * While the rocket ship button is being held, spin cargo belt motors to expel cargo
     * out at a slightly lower speed into the lower rocket ship level
     *
     * (this may take some driver practice to get working reliably)
     */
    if(m_manipController.getRawButton(2))
    {
      m_cargoBelt.set(CARGO_BELT_RKT_SPEED);
    }
    else if(m_manipController.getRawButtonReleased(2))
    {
      m_cargoBelt.set(0);
    }

    /*
     * Code here to handle button presses for hatch panel grab/release
     * NOTE: it may be necessary to consider that the motor should only turn
     *       a set number of degrees and stop, or alternatively run only until
     *       it reaches the stop position.
     *
     * (this may take some driver practice to get working reliably)
     */

    /*
     * Negative signs are here because the values from the analog sticks are backwards
     * from what we want. Pushing the stick forward returns a negative when we want a
     * positive value sent to the wheels.
     *
     * If you want to change the joystick axis used, open the driver station, go to the
     * USB tab, and push the sticks determine their axis numbers
     *
     * This was set up with a logitech controller, note there is a switch on the back of the
     * controller that changes how it functions
     */
    m_drivetrain.arcadeDrive(-m_driverController.getRawAxis(1), -m_driverController.getRawAxis(4), false);
  }
}

/*
 * The kit of parts drivetrain is known as differential drive, tank drive or skid-steer drive.
 *
 * There are two common ways to control this drivetrain: Arcade and Tank
 *
 * Arcade allows one stick to be pressed forward/backwards to power both sides of the drivetrain to move straight forwards/backwards.
 * A second stick (or the second axis of the same stick) can be pushed left/right to turn the robot in place.
 * When one stick is pushed forward and the other is pushed to the side, the robot will power the drivetrain
 * such that it both moves forwards and turns, turning in an arch.
 *
 * Tank drive allows a single stick to control of a single side of the robot.
 * Push the left stick forward to power the left side of the drive train, causing the robot to spin around to the right.
 * Push the right stick to power the motors on the right side.
 * Push both at equal distances to drive forwards/backwards and use at different speeds to turn in different arcs.
 * Push both sticks in opposite directions to spin in place.
 *
 * arcadeDrive can be replaced with tankDrive like so:
 *
 * m_drivetrain.tankDrive(-m_driverController.getRawAxis(1), -m_driverController.getRawAxis(5))
 *
 * Inputs can be squared which decreases the sensitivity of small drive inputs.
 *
 * It literally just takes (your inputs * your inputs), so a 50% (0.5) input from the controller becomes (0.5 * 0.5) -> 0.25
 *
 * This is an option that can be passed into arcade or tank drive:
 * arcadeDrive(double xSpeed, double zRotation, boolean squareInputs)
 *
 *
 * For more information see:
 * https://docs.wpilib.org/en/stable/docs/software/hardware-apis/motors/wpi-drive-classes.html
 *
 * https://github.com/wpilibsuite/allwpilib/blob/main/wpilibj/src/main/java/edu/wpi/first/wpilibj/drive/DifferentialDrive.java
 *
 */
