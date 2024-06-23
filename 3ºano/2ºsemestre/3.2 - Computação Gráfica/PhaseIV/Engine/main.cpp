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

std::map<std::string, GLuint[3]> buffers2;

using namespace std;
using namespace tinyxml2;

World world;
bool axes = false;
bool lines = false;
float alpha, betA, radius;
// GLuint vbo;
GLuint buffers[3];
int startX, startY, tracking = 0;

/*void transform(Color *color) {
    glColor3d(color->x, color->y, color->z);
}*/

void initVBO(const ModelData &modelData)
{
    // Gera e associa buffers
    GLuint vertexVBO, normalVBO, textureVBO;

    // Buffer de vértices
    glGenBuffers(1, &vertexVBO);
    glBindBuffer(GL_ARRAY_BUFFER, vertexVBO);
    glBufferData(GL_ARRAY_BUFFER, modelData.vertexPoints.size() * sizeof(Point2), modelData.vertexPoints.data(), GL_STATIC_DRAW);

    // Buffer de normais
    glGenBuffers(1, &normalVBO);
    glBindBuffer(GL_ARRAY_BUFFER, normalVBO);
    glBufferData(GL_ARRAY_BUFFER, modelData.normalPoints.size() * sizeof(Point2), modelData.normalPoints.data(), GL_STATIC_DRAW);

    // Buffer de texturas
    glGenBuffers(1, &textureVBO);
    glBindBuffer(GL_ARRAY_BUFFER, textureVBO);
    glBufferData(GL_ARRAY_BUFFER, modelData.texturePoints.size() * sizeof(Point3), modelData.texturePoints.data(), GL_STATIC_DRAW);

    // Guardar buffers
    buffers[0] = vertexVBO;
    buffers[1] = normalVBO;
    buffers[2] = textureVBO;
    glBindBuffer(GL_ARRAY_BUFFER, 0);
}

void recursivaVBO(const Group& group) {
    for (const Model& model : group.models) {
        initVBO(world.modelData[model.file]);
        std::array<GLuint, 3> bufferIds;
        bufferIds.fill(0); // Preenche o array com valores padrão
        bufferIds[0] = buffers[0];
        bufferIds[1] = buffers[1];
        bufferIds[2] = buffers[2];
        for (int i = 0; i < 3; ++i) {
            // Copia os elementos individualmente
            buffers2[model.file][i] = bufferIds[i];
        }
    }
    for (const Group& subGroup : group.groups) {
        recursivaVBO(subGroup);
    }
}


void initGL()
{
    glewInit();
    glEnableClientState(GL_VERTEX_ARRAY);
    glEnableClientState(GL_NORMAL_ARRAY);
    glEnableClientState(GL_TEXTURE_COORD_ARRAY);

    // OpenGL settings
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    // Init lighting
    glEnable(GL_LIGHTING);

	glEnable(GL_LIGHT0);
    glEnable(GL_LIGHT1);
    glEnable(GL_LIGHT2);
    glEnable(GL_LIGHT3);
    glEnable(GL_LIGHT4);
    glEnable(GL_LIGHT5);
    glEnable(GL_LIGHT6);
    glEnable(GL_LIGHT7);
    glEnable(GL_RESCALE_NORMAL);
    glEnable(GL_TEXTURE_2D);

    float amb[4] = { 1.0f, 1.0f, 1.0f, 1.0f };
	glLightModelfv(GL_LIGHT_MODEL_AMBIENT, amb);

    // Verify the first group and its models
    if (world.groups.empty()) {
        cout << "Error: No groups found in the world\n";
        return;
    }

    for (const Group& group : world.groups) {
        recursivaVBO(group);
    }
}


void animatedTranslate(Group group)
{
    float yBefore[3] = {0, 1, 0};
    float turnCount = 0;

    float pos[3], deriv[3], Z[3], m[16];

    // para calcular a curva
    renderCatmullRomCurve(group.catmullCurves[0].controlPoints);

    float elapsedTime = glutGet(GLUT_ELAPSED_TIME) / 1000.0;
    float actualTime = elapsedTime - group.transform.translate.time * turnCount;

    if (actualTime > group.transform.translate.time)
    {
        turnCount++;
        actualTime = elapsedTime - group.transform.translate.time * turnCount;
    }
    float gt = actualTime / group.transform.translate.time;

    // para ele movimentar se
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

void animatedRotate(Group group)
{
    float elapsedTime = glutGet(GLUT_ELAPSED_TIME);
    float angle = 360 / (group.transform.rotate.time * 1000);
    glRotatef(elapsedTime * angle, group.transform.rotate.x, group.transform.rotate.y, group.transform.rotate.z);
}

void drawGroups(const Group &group)
{
    glPushMatrix();

    for (string order : group.transform.order)
    {
        if (order == "rotate")
        {
            if (group.transform.rotate.time != -1)
            {
                animatedRotate(group);
            }
            else
            {
                glRotatef(group.transform.rotate.angle, group.transform.rotate.x, group.transform.rotate.y, group.transform.rotate.z);
            }
        }
        else if (order == "translate")
        {
            if (group.transform.translate.time != 0 && group.transform.translate.align)
            {
                animatedTranslate(group);
            }
            else
            {
                glTranslatef(group.transform.translate.x, group.transform.translate.y, group.transform.translate.z);
            }
        }
        else if (order == "scale")
        {
            glScalef(group.transform.scale.x, group.transform.scale.y, group.transform.scale.z);
        }
    }
    


    glColor3f(1, 1, 1);

    // Draw models
    for (const auto &model : group.models)
    {
        if (world.modelData.find(model.file) == world.modelData.end())
        {
            printf("Model file not found: %s\n", model.file.c_str());
            continue;
        }

        const auto &bufferIds = buffers2[model.file];
        const auto &modelData = world.modelData[model.file];

        GLfloat diffuse[] = {model.color.diffuse.R / 255.0f, model.color.diffuse.G / 255.0f, model.color.diffuse.B / 255.0f, 1.0f};
        GLfloat emissive[] = {model.color.emissive.R / 255.0f, model.color.emissive.G / 255.0f, model.color.emissive.B / 255.0f, 1.0f};
        GLfloat specular[] = {model.color.specular.R / 255.0f, model.color.specular.G / 255.0f, model.color.specular.B / 255.0f, 1.0f};
        GLfloat ambient[] = {model.color.ambient.R / 255.0f, model.color.ambient.G / 255.0f, model.color.ambient.B / 255.0f, 1.0f};

        glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse);
        glMaterialfv(GL_FRONT, GL_SPECULAR, specular);
        glMaterialfv(GL_FRONT, GL_EMISSION, emissive);
        glMaterialfv(GL_FRONT, GL_AMBIENT, ambient);
        glMaterialf(GL_FRONT, GL_SHININESS, model.color.shininess);

        if (model.textureId !=0) {
        glBindTexture(GL_TEXTURE_2D, model.textureId );

        // Bind buffers
        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[0]);
        glVertexPointer(3, GL_FLOAT, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[1]);
        glNormalPointer(GL_FLOAT, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[2]);
        glTexCoordPointer(2, GL_FLOAT, 0, 0);

        glDrawArrays(GL_TRIANGLES, 0, modelData.vertexPoints.size());
            glBindTexture(GL_TEXTURE_2D, 0);

    }
    else {
        // Bind buffers
        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[0]);
        glVertexPointer(3, GL_FLOAT, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[1]);
        glNormalPointer(GL_FLOAT, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[2]);
        glTexCoordPointer(2, GL_FLOAT, 0, 0);
        glDrawArrays(GL_TRIANGLES, 0, modelData.vertexPoints.size());
    }
    }
    

    // Draw subgroups
    for (const auto &subGroup : group.groups)
    {
        drawGroups(subGroup);
    }

    glPopMatrix();
}


void drawAxes()
{
    glDisable(GL_LIGHTING);
    glBegin(GL_LINES);
    glColor3f(1.5, 0.0, 0.0);
    glVertex3f(-15, 0.0, 0.0);
    glVertex3f(15, 0.0, 0.0);
    glEnd();

    glBegin(GL_LINES);
    glColor3f(0.0, 1.5, 0.0);
    glVertex3f(0.0, -15, 0.0);
    glVertex3f(0.0, 15, 0.0);
    glEnd();

    glBegin(GL_LINES);
    glColor3f(0.0, 0.0, 1.5);
    glVertex3f(0.0, 0.0, -15);
    glVertex3f(0.0, 0.0, 15);
    glEnd();
    glEnable(GL_LIGHTING);
}

void processKeys(unsigned char key, int x, int y)
{
    switch (key)
    {
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

void processSpecialKeys(int key, int x, int y)
{
    switch (key)
    {
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
void renderScene()
{   int i = 0;
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glLoadIdentity();

    gluLookAt(world.camera.position[0], world.camera.position[1], world.camera.position[2],
                world.camera.lookAt[0], world.camera.lookAt[1], world.camera.lookAt[2],
                world.camera.up[0], world.camera.up[1], world.camera.up[2]);

    if (lines)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    else
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    
        for (Light l : world.lights) {

            float amb[4]  = {0.2f, 0.2f, 0.2f, 1.0f};
            float diff[4] = {1.0f, 1.0f, 1.0f, 1.0f};
            float spec[4] = {1.0f, 1.0f, 1.0f, 1.0f};

            float position[4] = {l.position.x, l.position.y, l.position.z, 1.0f};
            float direction[4] = {l.direction.x, l.direction.y, l.direction.z, 0.0f};
            glLightfv(GL_LIGHT0 + i, GL_AMBIENT, amb);
            glLightfv(GL_LIGHT0 + i, GL_DIFFUSE, diff);
            glLightfv(GL_LIGHT0 + i, GL_SPECULAR, spec);

            if (l.type == "point") {
                glLightfv(GL_LIGHT0 + i, GL_POSITION, position);
            }

            if (l.type == "directional") {
                glLightfv(GL_LIGHT0 + i, GL_POSITION, direction);
            }

            if (l.type == "spot") {
                glLightfv(GL_LIGHT0 + i, GL_POSITION, position);
                glLightfv(GL_LIGHT0 + i, GL_SPOT_DIRECTION, direction);
                glLightf(GL_LIGHT0 + i, GL_SPOT_CUTOFF, l.cutoff);
            }

            i++;
        }

        initGL();

    for (const auto &group : world.groups)
    {
        drawGroups(group);
    }


    if (axes)
        drawAxes();

    char s[32];

    frame++;
    timeframe = glutGet(GLUT_ELAPSED_TIME);
    if (timeframe - timebase > 1000)
    {
        frames = frame * 1000.0 / (timeframe - timebase);
        timebase = timeframe;
        frame = 0;
        sprintf(s, "CG@DI-UM %f fps", frames);
        glutSetWindowTitle(s);
    }
    glutSwapBuffers();
}


void changeSize(int w, int h)
{
    if (h == 0)
        h = 1;
    float ratio = w * 1.0 / h;

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();

    glViewport(0, 0, w, h);
    gluPerspective(world.camera.projection[0], ratio, world.camera.projection[1], world.camera.projection[2]);

    glMatrixMode(GL_MODELVIEW);
}

void processMouseButtons(int button, int state, int xx, int yy)
{

    if (state == GLUT_DOWN)
    {
        startX = xx;
        startY = yy;
        if (button == GLUT_LEFT_BUTTON)
            tracking = 1;
        else if (button == GLUT_RIGHT_BUTTON)
            tracking = 2;
        else
            tracking = 0;
    }
    else if (state == GLUT_UP)
    {
        if (tracking == 1)
        {
            alpha += (xx - startX);
            betA += (yy - startY);
        }
        else if (tracking == 2)
        {

            radius -= yy - startY;
            if (radius < 3)
                radius = 3.0;
        }
        tracking = 0;
    }
}

void processMouseMotion(int xx, int yy)
{

    int deltaX, deltaY;
    int alphaAux, betaAux;
    int rAux;

    if (!tracking)
        return;

    deltaX = xx - startX;
    deltaY = yy - startY;

    if (tracking == 1)
    {

        alphaAux = alpha + deltaX;
        betaAux = betA + deltaY;

        if (betaAux > 85.0)
            betaAux = 85.0;
        else if (betaAux < -85.0)
            betaAux = -85.0;

        rAux = radius;
    }
    else if (tracking == 2)
    {

        alphaAux = alpha;
        betaAux = betA;
        rAux = radius - deltaY;
        if (rAux < 3)
            rAux = 3;
    }
    world.camera.position[0] = rAux * sin(alphaAux * 3.14 / 180.0) * cos(betaAux * 3.14 / 180.0);
    world.camera.position[1] = rAux * cos(alphaAux * 3.14 / 180.0) * cos(betaAux * 3.14 / 180.0);
    world.camera.position[2] = rAux * sin(betaAux * 3.14 / 180.0);
}

int main(int argc, char **argv)
{
    // Load XML and models
    if (argc < 2)
    {
        cout << "Usage: " << argv[0] << " <input_file>\n";
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



    timebase = glutGet(GLUT_ELAPSED_TIME);
    if (!parseXML(argv[1], &world, radius, betA, alpha))
    {
        cout << "Error parsing input file\n";
        return 1;
    }
        cout << "Number of groups: " << world.groups.size() << endl;
    if (!world.groups.empty()) {
        cout << "Number of models in the first group: " << world.groups[0].models.size() << endl;
    }

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    glutMainLoop();

    return 0;
}