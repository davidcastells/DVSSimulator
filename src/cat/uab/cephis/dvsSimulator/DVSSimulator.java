/*
 * Copyright (C) 2019 Universitat Autonoma de Barcelona - David Castells-Rufas <david.castells@uab.cat>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cat.uab.cephis.dvsSimulator;

import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimationFactory;
import com.jme3.animation.LoopMode;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.ResetStatsState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.PlayState;
import com.jme3.cinematic.events.AnimationEvent;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.shadow.SpotLightShadowRenderer;
import com.jme3.system.AppSettings;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dcr
 */
public class DVSSimulator  extends SimpleApplication {

    private static ManualTimer manualTimer;

   
    Cinematic cinematic;
    
    public boolean generateLeft = true;
    
    private double stopTime;
    
    
    public static void main(String... argv)
    {
        double seconds = 6.5;
        double fps = 1000;
        
        DVSSimulator app = new DVSSimulator(true, seconds, fps);
        
        app.start();
        app.waitUntilEnd();
        
        app = new DVSSimulator(false, seconds, fps);
        app.start();
    }
    private EventWriterPostProcessor writer;
    

    public DVSSimulator(boolean left, double stopTime, double fps)
    {
        showSettings = false;
        AppSettings settings = new AppSettings(true);
        settings.setResolution(320, 240);
        //settings.setFrameRate(20);
        
        setStopTime(stopTime);
        setSettings(settings);
        manualTimer = new ManualTimer(fps);
        
        setTimer(manualTimer);
    }

    /**
     * Overriden to control the timer 
     */
    @Override
    public void update()
    {
        super.update(); //To change body of generated methods, choose Tools | Templates.
        
        if (manualTimer.getTimeInSeconds() >= stopTime)
        {
            writer.cleanup();
            stop();
        }
    }
    
    
    
    
    
    @Override
    public void simpleInitApp() {
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        stateManager.detach(stateManager.getState(ResetStatsState.class));
        stateManager.detach(stateManager.getState(DebugKeysAppState.class));
        stateManager.detach(stateManager.getState(StatsAppState.class));
        final Node jaime = LoadModel();
        
        setupLights();        
        setupCamera();
        //setupFloor();
        setupCallibrationBoxes();
        setupCinematic(jaime);
        setupInput();
        
        setupEventWriter();
    }
    
    public Node LoadModel() {
        Node jaime = (Node)assetManager.loadModel("Models/Jaime/Jaime.j3o");
        jaime.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(jaime);
        return jaime;
    }

    public void setupLights() {
        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        rootNode.addLight(al);
        
        SpotLight sl = new SpotLight();
        sl.setColor(ColorRGBA.White.mult(1.0f));
        sl.setPosition(new Vector3f(1.2074411f, 10.6868908f, 4.1489987f));
        sl.setDirection(sl.getPosition().mult(-1)); 
        sl.setSpotOuterAngle(0.1f);
        sl.setSpotInnerAngle(0.004f);      
        rootNode.addLight(sl);
        
        //pointlight to fake indirect light coming from the ground
        PointLight pl = new PointLight();
        pl.setColor(ColorRGBA.White.mult(1.5f));
        pl.setPosition(new Vector3f(0, 0, 1));
        pl.setRadius(2);
        rootNode.addLight(pl);
        
        SpotLightShadowRenderer shadows = new SpotLightShadowRenderer(assetManager, 1024);
        shadows.setLight(sl);
        shadows.setShadowIntensity(0.3f);
        shadows.setEdgeFilteringMode(EdgeFilteringMode.PCF8);
        viewPort.addProcessor(shadows);

        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        SSAOFilter filter = new SSAOFilter(0.10997847f,0.440001f,0.39999998f,-0.008000026f);;
        fpp.addFilter(filter);
        fpp.addFilter(new FXAAFilter());
        fpp.addFilter(new FXAAFilter());     
        
        viewPort.addProcessor(fpp);
    }

    public void setupEventWriter()
    {
        try
        {
            File dir = new File("C:\\Projects\\Cephis\\INT_2017_CHRONOCAM\\dataset");

            File file;
            
            if (generateLeft)
                file = new File(dir, "jaime_left_td.dat");
            else
                file = new File(dir, "jaime_right_td.dat");
            
            writer = new EventWriterPostProcessor(settings.getWidth(), settings.getHeight(), file, manualTimer);
            
            viewPort.addProcessor(writer);
        } catch (IOException ex)
        {
            Logger.getLogger(DVSSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public void setupCamera() {
         flyCam.setEnabled(false);
         
    }

    public void setupCinematic(final Node jaime) {
        cinematic = new Cinematic(rootNode, 60);
        stateManager.attach(cinematic);
        
        jaime.move(0, 0, -3);
        
        AnimationFactory af = new AnimationFactory(0.7f, "JumpForward");
        af.addTimeTranslation(0, new Vector3f(0, 0, -3));
        af.addTimeTranslation(0.35f, new Vector3f(0, 1, -1.5f));
        af.addTimeTranslation(0.7f, new Vector3f(0, 0, 0));
        jaime.getControl(AnimControl.class).addAnim(af.buildAnimation());
   
        cinematic.enqueueCinematicEvent(new AnimationEvent(jaime, "Idle",3, LoopMode.DontLoop));
        float jumpStart = cinematic.enqueueCinematicEvent(new AnimationEvent(jaime, "JumpStart", LoopMode.DontLoop));
        cinematic.addCinematicEvent(jumpStart+0.2f, new AnimationEvent(jaime, "JumpForward", LoopMode.DontLoop,1));        
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "JumpEnd", LoopMode.DontLoop));                
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "Punches", LoopMode.DontLoop));
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "SideKick", LoopMode.DontLoop));        
        float camStart = cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "Taunt", LoopMode.DontLoop));
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "Idle",1, LoopMode.DontLoop));
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "Wave", LoopMode.DontLoop));
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "Idle", LoopMode.DontLoop));        
        
//        CameraNode camNode = cinematic.bindCamera("cam", cam);
//        
//        if (generateLeft)
//            // left
//            camNode.setLocalTranslation(new Vector3f(1.1f, 1.2f, 2.9f));
//        else
//            // right
//            camNode.setLocalTranslation(new Vector3f(1.1f, 1.2f, 2.9f));
//        
//        camNode.lookAt(new Vector3f(0, 0.5f, 0), Vector3f.UNIT_Y);
        
        if (generateLeft)
        {
            cam.setLocation(new Vector3f(0f, 0, 3f)); //2.9
            cam.lookAt(new Vector3f(0, 0.5f, 0), Vector3f.UNIT_Y);
        }
        else
        {
            cam.setLocation(new Vector3f(0f, 0f, 3f));
            cam.lookAt(new Vector3f(0, 0.5f, 0), Vector3f.UNIT_Y);
            cam.setLocation(new Vector3f(0.3f, 0, 3f));
        }
          
        
          
//        MotionPath path = new MotionPath();
//        path.addWayPoint(new Vector3f(1.1f, 1.2f, 2.9f));
//        path.addWayPoint(new Vector3f(0f, 1.2f, 3.0f));
//        path.addWayPoint(new Vector3f(-1.1f, 1.2f, 2.9f));        
//        path.enableDebugShape(assetManager, rootNode);
//        path.setCurveTension(0.8f);
//        
//        MotionEvent camMotion = new MotionEvent(camNode, path,6);
//        camMotion.setDirectionType(MotionEvent.Direction.LookAt);
//        camMotion.setLookAt(new Vector3f(0, 0.5f, 0), Vector3f.UNIT_Y);
//        cinematic.addCinematicEvent(camStart, camMotion);
        cinematic.activateCamera(0, "cam");
       
        
        cinematic.fitDuration();
        cinematic.setSpeed(1.2f);
//        cinematic.setSpeed(0.1f);
        cinematic.setLoopMode(LoopMode.Loop);
        cinematic.play();
    }

    public void setupFloor() {
        Quad q = new Quad(20, 20);
       q.scaleTextureCoordinates(Vector2f.UNIT_XY.mult(10));
       Geometry geom = new Geometry("floor", q);
       Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
       mat.setColor("Diffuse", ColorRGBA.White);
       mat.setColor("Specular", ColorRGBA.White);
       mat.setColor("Ambient", ColorRGBA.Black);
       mat.setBoolean("UseMaterialColors", true);
       mat.setFloat("Shininess", 0);
       geom.setMaterial(mat);

       geom.rotate(-FastMath.HALF_PI, 0, 0);
       geom.center();
       geom.setShadowMode(RenderQueue.ShadowMode.Receive);
       rootNode.attachChild(geom);
    }
    
    private void setupCallibrationBoxes()
    {
        Geometry cube = new Geometry("blue cube", new Box(20, 1, 1));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        cube.setMaterial(mat);
        
        cube.setLocalTranslation(-5, -5, -20);
//        cube.rotate(0, 0, (float) Math.PI/8);
        
        rootNode.attachChild(cube);
        
        cube = new Geometry("blue cube", new Box(20, 1, 1));
        cube.setMaterial(mat);
        
        cube.setLocalTranslation(-5, 14, -20);
//        cube.rotate(0, 0, (float) Math.PI/8);
        
        rootNode.attachChild(cube);
    }

    

    public void setupInput() {
        inputManager.addMapping("start", new KeyTrigger(KeyInput.KEY_PAUSE));
        inputManager.addListener(new ActionListener() {

            public void onAction(String name, boolean isPressed, float tpf) {
                if(name.equals("start") && isPressed){
                    if(cinematic.getPlayState() != PlayState.Playing){                                                
                        cinematic.play();
                    }else{
                        cinematic.pause();
                    }
                }
            }
        }, "start");
    }

    private void setStopTime(double d)
    {
        this.stopTime = d;
    }

    private void waitUntilEnd()
    {
        // first wait for its creation
        while (!getContext().isCreated())
        {
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException ex)
            {
                Logger.getLogger(DVSSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        System.out.println("Context Creation Confirmed!");
        
        // not wait for its dead
        while (getContext().isCreated())
        {
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException ex)
            {
                Logger.getLogger(DVSSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        System.out.println("Context dead Confirmed!");
    }

    

    

   
    
}
