#include <cstdio>
#define _USE_MATH_DEFINES
#include <cstring>

#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <cmath>
#include <algorithm>
#include <map>
#include "catmullrom.h"
#include "parser.h"

static int timeframe = 0;
static int timebase;
static float frames;
static int frame;

using namespace std;
using namespace tinyxml2;

World world;
bool axes = false;
bool lines = false;
float alpha, betA, radius;
GLuint vbo;

int startX, startY, tracking = 0;

void transform(Color *color) {
    glColor3d(color->x, color->y, color->z);
}

void drawPoints(string filename) {
    vector<Point> pontos = world.points[filename];

    // Create a buffer and set it as the current one
    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);

    // Upload vertex data to the buffer
    glBufferData(GL_ARRAY_BUFFER, pontos.size() * sizeof(Point), &pontos[0], GL_STATIC_DRAW);

    // Define the layout of our vertex data
    glEnableClientState(GL_VERTEX_ARRAY);
    glVertexPointer(3, GL_FLOAT, sizeof(Point), 0);

    // Draw the object
    glDrawArrays(GL_TRIANGLES, 0, pontos.size());

    // Clean up
    glDisableClientState(GL_VERTEX_ARRAY);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glDeleteBuffers(1, &vbo);
}


void animatedTranslate(Group group){
    float yBefore[3] = { 0,1,0 };
    float turnCount = 0;

    float pos[3], deriv[3], Z[3], m[16];

    //para calcular a curva
    renderCatmullRomCurve(group.catmullCurves[0].controlPoints);

    float elapsedTime = glutGet(GLUT_ELAPSED_TIME) / 1000.0;
    float actualTime = elapsedTime - group.transform.translate.time * turnCount;

    if(actualTime > group.transform.translate.time){
        turnCount++;
        actualTime = elapsedTime - group.transform.translate.time*turnCount;
    }
    float gt = actualTime/group.transform.translate.time;

    //para ele movimentar se
    getGlobalCatmullRomPoint(gt, pos, deriv, group.catmullCurves[0].controlPoints);

    normalize(deriv);

    // Z = X x Yi-1
    cross(deriv, yBefore, Z);
    normalize(Z);

    // Yi = Z x X
    float newY[3]{0, 0, 0};
    cross(Z, deriv, newY);
    normalize(newY);

    buildRotMatrix(deriv, newY, Z, m);

    glTranslatef(pos[0], pos[1], pos[2]);
    glMultMatrixf(m);
}

void animatedRotate(Group group){
    float elapsedTime = glutGet(GLUT_ELAPSED_TIME);
    float angle = 360/(group.transform.rotate.time * 1000);
    glRotatef(elapsedTime * angle, group.transform.rotate.x, group.transform.rotate.y, group.transform.rotate.z);
}

void drawGroups(Group group) {
    glPushMatrix();
    // Aplicar transformações do grupo

    if (group.transform.translate.time != 0 && group.transform.translate.align){
        animatedTranslate(group);
    }
    else
        glTranslatef(group.transform.translate.x, group.transform.translate.y, group.transform.translate.z);

    glScalef(group.transform.scale.x, group.transform.scale.y, group.transform.scale.z);
    if (group.transform.rotate.time != -1){
        animatedRotate(group);
    } else
        glRotatef(group.transform.rotate.angle, group.transform.rotate.x, group.transform.rotate.y, group.transform.rotate.z);


    for (const auto &model : group.models) {
        glPushMatrix();
        glTranslatef(model.translate.x, model.translate.y, model.translate.z);
        if (group.transform.color != nullptr) {
            transform(group.transform.color);
        }
        drawPoints(model.file);
        glPopMatrix();
    }

    for (const auto &childGroup : group.groups) {
        drawGroups(childGroup);
    }

    glPopMatrix();
}


void drawAxes() {
    glBegin(GL_LINES);
    glColor3f(1.5, 0.0, 0.0);
    glVertex3f(-15, 0.0, 0.0);
    glVertex3f(15, 0.0, 0.0);

    glColor3f(0.0, 1.5, 0.0);
    glVertex3f(0.0, -15, 0.0);
    glVertex3f(0.0, 15, 0.0);

    glColor3f(0.0, 0.0, 1.5);
    glVertex3f(0.0, 0.0, -15);
    glVertex3f(0.0, 0.0, 15);
    glEnd();
}

void processKeys(unsigned char key, int x, int y) {
    switch (key) {
        case 'l':
            lines = !lines;
            break;
        case 'a':
            axes = !axes;
            break;
        case 'i':
            radius -= 1;
            if (radius < 0)
                radius = 0;
            break;
        case 'o':
            radius += 1;
            break;
    }

    world.camera.position[0] = radius * cos(betA) * sin(alpha);
    world.camera.position[1] = radius * sin(betA);
    world.camera.position[2] = radius * cos(betA) * cos(alpha);
    glutPostRedisplay();
}

void processSpecialKeys(int key, int x, int y) {
    switch (key) {
        case GLUT_KEY_LEFT:
            alpha -= M_PI / 30;
            break;
        case GLUT_KEY_RIGHT:
            alpha += M_PI / 30;
            break;
        case GLUT_KEY_UP:
            betA += M_PI / 30;
            if (betA >= M_PI / 2)
                betA = M_PI / 2 - 0.001;
            break;
        case GLUT_KEY_DOWN:
            betA -= M_PI / 30;
            if (betA <= -M_PI / 2)
                betA = -M_PI / 2 + 0.001;
            break;
    }

    world.camera.position[0] = radius * cos(betA) * sin(alpha);
    world.camera.position[1] = radius * sin(betA);
    world.camera.position[2] = radius * cos(betA) * cos(alpha);
    glutPostRedisplay();
}

void renderScene() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity();

    gluLookAt(world.camera.position[0], world.camera.position[1], world.camera.position[2],
              world.camera.lookAt[0], world.camera.lookAt[1], world.camera.lookAt[2],
              world.camera.up[0], world.camera.up[1], world.camera.up[2]);

    if (lines)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    else
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

    if (axes)
        drawAxes();


    for (const auto &group : world.groups) {
        drawGroups(group);
    }



    char s[32];

    frame++;
    timeframe = glutGet(GLUT_ELAPSED_TIME);
    if (timeframe - timebase > 1000) {
        frames = frame*1000.0 / (timeframe - timebase);
        timebase = timeframe;
        frame = 0;
        sprintf(s, "CG@DI-UM %f fps", frames);
        glutSetWindowTitle(s);
    }
    glutSwapBuffers();
}

void changeSize(int w, int h) {
    if (h == 0)
        h = 1;
    float ratio = w * 1.0 / h;

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();

    glViewport(0, 0, w, h);
    gluPerspective(world.camera.projection[0], ratio, world.camera.projection[1], world.camera.projection[2]);

    glMatrixMode(GL_MODELVIEW);
}


void processMouseButtons(int button, int state, int xx, int yy) {

    if (state == GLUT_DOWN)  {
        startX = xx;
        startY = yy;
        if (button == GLUT_LEFT_BUTTON)
            tracking = 1;
        else if (button == GLUT_RIGHT_BUTTON)
            tracking = 2;
        else
            tracking = 0;
    }
    else if (state == GLUT_UP) {
        if (tracking == 1) {
            alpha += (xx - startX);
            betA += (yy - startY);
        }
        else if (tracking == 2) {

            radius -= yy - startY;
            if (radius < 3)
                radius = 3.0;
        }
        tracking = 0;
    }
}


void processMouseMotion(int xx, int yy) {

    int deltaX, deltaY;
    int alphaAux, betaAux;
    int rAux;

    if (!tracking)
        return;

    deltaX = xx - startX;
    deltaY = yy - startY;

    if (tracking == 1) {


        alphaAux = alpha + deltaX;
        betaAux = betA + deltaY;

        if (betaAux > 85.0)
            betaAux = 85.0;
        else if (betaAux < -85.0)
            betaAux = -85.0;

        rAux = radius;
    }
    else if (tracking == 2) {

        alphaAux = alpha;
        betaAux = betA;
        rAux = radius - deltaY;
        if (rAux < 3)
            rAux = 3;
    }
    world.camera.position[0] = rAux * sin(alphaAux * 3.14 / 180.0) * cos(betaAux * 3.14 / 180.0);
    world.camera.position[1] = rAux * cos(alphaAux * 3.14 / 180.0) * cos(betaAux * 3.14 / 180.0);
    world.camera.position[2] = rAux * 							     sin(betaAux * 3.14 / 180.0);
}


int main(int argc, char **argv) {
    // Load XML and models
    if (argc < 2) {
        cout << "Usage: " << argv[0] << " <input_file>\n";
        return 1;
    }

    if (!parseXML(argv[1], &world, radius, betA, alpha)) {
        cout << "Error parsing input file\n";
        return 1;
    }

    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DEPTH | GLUT_DOUBLE | GLUT_RGBA);
    glutInitWindowPosition(100, 100);
    glutInitWindowSize(800, 800);
    glutCreateWindow("Engine");

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    glutDisplayFunc(renderScene);
    glutIdleFunc(renderScene);
    glutReshapeFunc(changeSize);

    glutKeyboardFunc(processKeys);
    glutSpecialFunc(processSpecialKeys);
    glutMouseFunc(processMouseButtons);
    glutMotionFunc(processMouseMotion);

    // init GLEW
#ifndef __APPLE__
    glewInit();
#endif
    glEnableClientState(GL_VERTEX_ARRAY);

    timebase = glutGet(GLUT_ELAPSED_TIME);


    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    glutMainLoop();

    return 0;
}