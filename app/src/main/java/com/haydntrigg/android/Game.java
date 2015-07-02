package com.haydntrigg.android;
import android.opengl.GLES20;
import android.view.MotionEvent;
import android.widget.TextView;

import com.haydntrigg.game.supersovietsheep.R;

import org.joml.Matrix4f;

import org.jbox2d.dynamics.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by HAYDN on 1/7/2015.
 */
public class Game {

    private MainActivity Parent = null;
    TextView textView;
    int Width,Height;
    Matrix4f View = new Matrix4f();
    Texture logoTexture;
    Sprite boxSprite;
    Texture groundTexture;
    Sprite groundSprite;
    World b2World;

    enum ObjectType
    {
        Box,
        Floor
    }


    Game(MainActivity parent)
    {
        Parent = parent;
        textView = (TextView)Parent.findViewById(R.id.frame_rate);

        b2World = new World(new Vec2(0.0f,-9.81f));

        CreateFloor(new Vec2(6.5f, 1f));
        CreateBox(new Vec2(6.5f,8.75f));
        CreateBox(new Vec2(6.5f,7.0f));

    }

    private void CreateBox(Vec2 position)
    {
        BodyDef bodyDef = new BodyDef();

        bodyDef.position = position;
        bodyDef.angle = 45.0f;
        bodyDef.linearVelocity = new Vec2(0.0f,0.0f);
        bodyDef.angularVelocity = 0.0f;
        bodyDef.fixedRotation = false;
        bodyDef.active = true;
        bodyDef.bullet = false;
        bodyDef.allowSleep = true;
        bodyDef.gravityScale = 1.0f;
        bodyDef.linearDamping = 0.0f;
        bodyDef.angularDamping = 0.0f;
        bodyDef.userData = (Object)ObjectType.Box;
        bodyDef.type = BodyType.DYNAMIC;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f,0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.userData = null;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.05f;
        fixtureDef.density = 1.0f;
        fixtureDef.isSensor = false;

        Body body = b2World.createBody(bodyDef);
        body.createFixture(fixtureDef);
    }

    private void CreateFloor(Vec2 position)
    {
        BodyDef bodyDef = new BodyDef();

        bodyDef.position = position;
        bodyDef.angle = 0.0f;
        bodyDef.linearVelocity = new Vec2(0.0f,0.0f);
        bodyDef.angularVelocity = 0.0f;
        bodyDef.fixedRotation = false;
        bodyDef.active = true;
        bodyDef.bullet = false;
        bodyDef.allowSleep = true;
        bodyDef.gravityScale = 1.0f;
        bodyDef.linearDamping = 0.0f;
        bodyDef.angularDamping = 0.0f;
        bodyDef.userData = (Object)ObjectType.Floor;
        bodyDef.type = BodyType.KINEMATIC;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(5.0f,0.05f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.userData = null;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.05f;
        fixtureDef.density = 1.0f;
        fixtureDef.isSensor = false;

        Body body = b2World.createBody(bodyDef);
        body.createFixture(fixtureDef);
    }

    public void Init() {


        Square.InitSquare();

        // Enable 2D Textures
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Enable Culling
        //GLES20.glFrontFace(GLES20.GL_CCW);
        //GLES20.glEnable(GLES20.GL_CULL_FACE);
        //GLES20.glCullFace(GLES20.GL_BACK);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        logoTexture = new Texture(Parent.getApplicationContext(), R.drawable.box);
        boxSprite = new Sprite(logoTexture);

        groundTexture = new Texture(Parent.getApplicationContext(), R.drawable.ground);
        groundSprite = new Sprite(groundTexture);
        GLES20.glClearColor(235f / 255.0f, 235f / 255.0f, 255f / 255.0f, 255f / 255.0f);
    }



    public void Update(float delta)
    {
        b2World.step(delta, 5, 5);
    }

    public void Draw()
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        int num_objects = b2World.getBodyCount();
        if(num_objects >= 0)
        {
            Body body = b2World.getBodyList();
            for(int i=0;i<num_objects;i++)
            {
                switch((ObjectType)body.getUserData())
                {
                    case Box:
                        boxSprite.Draw(body.getWorldCenter(), body.getAngle(), 1.05f / 48.0f, View);
                        break;
                    case Floor:
                        groundSprite.Draw(body.getWorldCenter(), body.getAngle(), 1.05f / 100.0f, View);
                        break;
                }
                body = body.getNext();
            }
        }
    }

    public void UI()
    {
        textView.setText("JBox2D Box Example. Written by Haydn Trigg");
    }

    public void TouchEvent(MotionEvent e)
    {
        int num_bodies = b2World.getBodyCount();
        if(num_bodies > 11) {
            List<Body> bodies = new ArrayList<Body>();
            for (Body b = b2World.getBodyList(); b != null; b = b.getNext()) bodies.add(b);
            for (int i = bodies.size() - 1; i >= 0; i--) {
                Body b = bodies.get(i);
                if ((ObjectType) b.getUserData() == ObjectType.Box) {
                    b2World.destroyBody(b);
                    break;
                }
            }
        }
        if(e.getAction() == MotionEvent.ACTION_DOWN) CreateBox(new Vec2(13.0f * e.getX()/Width,13.0f * ((float)Height/(float)Width) * (1.0f - e.getY()/Height)));
    }

    public void SetSize(int width, int height) {
        Width = width;
        Height = height;

        final float height_ratio = ((float)height)/((float)width);
        final float base_units = 13f;
        final float pixels_per_unit = 100.0f;
        float virtual_width = base_units;
        float virtual_height = virtual_width * height_ratio;




        View = new Matrix4f().ortho(0,virtual_width,0,virtual_height,1,-1);
        //.orthoM(View,0,0,Width,0,Height,1,-1);
    }
}
