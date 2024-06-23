#include <iostream>
#include <vector>
#include "tinyxml2.h"
#ifdef __APPLE__
#include <GLUT/glut.h>
#else
#include <GL/glut.h>
#endif
#include <fstream>
#include <sstream>
#include <cmath>

using namespace std;
using namespace tinyxml2;

struct Point {
    float x, y, z;
};

struct Camera {
    float position[3], lookAt[3], up[3], projection[3];
};

struct World {
    Camera camera;
    vector<string> files;
    vector<Point> points;
};

World world;
bool axes = false;
bool lines = false;
float alpha, betA, radius;

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

void drawPrimitives() {
    glBegin(GL_TRIANGLES);
    for (const Point& pt : world.points) {
        glColor3f(static_cast<float>(rand()) / RAND_MAX,
                static_cast<float>(rand()) / RAND_MAX,
                static_cast<float>(rand()) / RAND_MAX);
        glVertex3f(pt.x, pt.y, pt.z);
    }
    glEnd();
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

    drawPrimitives();

    glutSwapBuffers();
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
            if (radius < 0) radius = 0;
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
            if (betA >= M_PI / 2) betA = M_PI / 2 - 0.001;
            break;
        case GLUT_KEY_DOWN:
            betA -= M_PI / 30;
            if (betA <= -M_PI / 2) betA = -M_PI / 2 + 0.001;
            break;
    }
    world.camera.position[0] = radius * cos(betA) * sin(alpha);
    world.camera.position[1] = radius * sin(betA);
    world.camera.position[2] = radius * cos(betA) * cos(alpha);
    glutPostRedisplay();
}

bool readFile(const string& filename) {
    string fullFilePath = "../Models/" + filename;
    ifstream infile(fullFilePath);
    if (!infile) return false;

    string line;
    while (getline(infile, line)) {
        istringstream iss(line);
        string triangleStr;
        while (getline(iss, triangleStr, '/')) {
            istringstream triangleIss(triangleStr);
            Point pt;
            char delimiter;
            if (!(triangleIss >> pt.x >> delimiter >> pt.y >> delimiter >> pt.z)) return false;
            world.points.push_back(pt);
        }
    }
    return true;
}

bool parseXML(const char* xmlFile) {
    XMLDocument xmlDoc;
    XMLError eResult = xmlDoc.LoadFile(xmlFile);

    if (eResult != XML_SUCCESS) {
        std::cerr << "Error loading XML file: " << xmlDoc.ErrorStr() << std::endl;
        return false;
    }

    XMLElement* pRoot = xmlDoc.FirstChildElement("world");
    if (pRoot == nullptr) {
        std::cerr << "Invalid XML format: No 'world' element found." << std::endl;
        return false;
    }

    // Parsing dos elementos da window
    XMLElement* pWindow = pRoot->FirstChildElement("window");
    if (pWindow) {
        int width, height;
        pWindow->QueryIntAttribute("width", &width);
        pWindow->QueryIntAttribute("height", &height);
    }
    else return false;

    // Parsing dos elementos da câmara
    XMLElement* pCamera = pRoot->FirstChildElement("camera");
    if (pCamera) {
        XMLElement* pPosition = pCamera->FirstChildElement("position");
        if (pPosition) {
            float x, y, z;
            pPosition->QueryFloatAttribute("x", &x);
            pPosition->QueryFloatAttribute("y", &y);
            pPosition->QueryFloatAttribute("z", &z);

            world.camera.position[0] = x;
            world.camera.position[1] = y;
            world.camera.position[2] = z;
        }

        XMLElement* pLookat = pCamera->FirstChildElement("lookAt");
        if (pLookat) {
            float x, y, z;
            pLookat->QueryFloatAttribute("x", &x);
            pLookat->QueryFloatAttribute("y", &y);
            pLookat->QueryFloatAttribute("z", &z);

            world.camera.lookAt[0] = x;
            world.camera.lookAt[1] = y;
            world.camera.lookAt[2] = z;
        }

        XMLElement* pUp = pCamera->FirstChildElement("up");
        if (pUp) {
            float x, y, z;
            pUp->QueryFloatAttribute("x", &x);
            pUp->QueryFloatAttribute("y", &y);
            pUp->QueryFloatAttribute("z", &z);

            world.camera.up[0] = x;
            world.camera.up[1] = y;
            world.camera.up[2] = z;
        }

        XMLElement* pProjection = pCamera->FirstChildElement("projection");
        if (pProjection) {
            float x, y, z;
            pProjection->QueryFloatAttribute("fov", &x);
            pProjection->QueryFloatAttribute("near", &y);
            pProjection->QueryFloatAttribute("far", &z);

            world.camera.projection[0] = x;
            world.camera.projection[1] = y;
            world.camera.projection[2] = z;
        }
    }

    // Parsing dos elementos group
    XMLElement* pGroup = pRoot->FirstChildElement("group");
    if (pGroup) {
        // Parsing models
        XMLElement* pModels = pGroup->FirstChildElement("models");
        if (pModels) {
            XMLElement* pModel = pModels->FirstChildElement("model");
            while(pModel != nullptr) {
                const char* strfile;

                strfile = pModel->Attribute("file");
                string namefile = strfile;
                world.files.push_back(namefile);

                if (!readFile(namefile)) {
                    return false;
                }

                pModel = pModel->NextSiblingElement();
            }
        }
    }
    radius = sqrt(pow(world.camera.position[0], 2) + pow(world.camera.position[1], 2) + pow(world.camera.position[2], 2));
    betA = asin(world.camera.position[1] / radius);
    alpha = asin(world.camera.position[0] / sqrt(pow(world.camera.position[2], 2) + pow(world.camera.position[0], 2)));


    return true;
}

void changeSize(int w, int h) {
    if(h == 0)
        h = 1;
    float ratio = w * 1.0 / h;
    glMatrixMode(GL_PROJECTION);

    glLoadIdentity();
    glViewport(0, 0, w, h);
    gluPerspective(world.camera.projection[0] ,ratio, world.camera.projection[1]  , world.camera.projection[2]);
    glMatrixMode(GL_MODELVIEW);
}

int main(int argc, char** argv) {
    if (argc < 2) {
        cout << "Usage: " << argv[0] << " <input_file>\n";
        return 1;
    }

    if (!parseXML(argv[1])) {
        cout << "Error parsing input file\n";
        return 1;
    }

    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DEPTH|GLUT_DOUBLE|GLUT_RGBA);
    glutInitWindowPosition(100,100);
    glutInitWindowSize(800,800);
    glutCreateWindow("Engine");


    glutDisplayFunc(renderScene);
    glutReshapeFunc(changeSize);

    glutKeyboardFunc(processKeys);
    glutSpecialFunc(processSpecialKeys);

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    glutMainLoop();

    return 0;
}
