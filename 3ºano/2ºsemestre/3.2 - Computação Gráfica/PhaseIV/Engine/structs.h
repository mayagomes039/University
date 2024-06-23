#include <vector>
#include <string>
#include <map>

using namespace std;

#ifndef ENGINE_STRUCTS_H
#define ENGINE_STRUCTS_H
struct Point {
    float x, y, z;
    float nx, ny, nz;
    float tx, ty;
    float r, g, b;
};

struct Point2 {
    float x, y, z;
};

struct Point3 {
    float x, y;
};

struct ModelData{
    std::vector<Point2> vertexPoints;
    std::vector<Point2> normalPoints;
    std::vector<Point3> texturePoints;
    int vertexCount ;
};



struct Primitive {
    std::vector<Point> points;

    // Number of vertices
    GLuint vertexCount;

    Primitive(const std::vector<Point>& points)
        : points(points), vertexCount(points.size()) {}
};

struct Camera {
    float position[3], lookAt[3], up[3], projection[3];
};

struct Transform {
    float angle, time, x, y, z;
    bool align;
    Transform() : angle(0),time(0), x(0), y(0), z(0), align(false) {}
};

struct Color
{
    float R, G, B;
    Color(float R = 1.0f, float G = 1.0f, float B = 1.0f) : R(R), G(G), B(B) {}
};

struct ColorProperties
{
    Color diffuse;
    Color ambient;
    Color specular;
    Color emissive;
    float shininess;

    ColorProperties() : diffuse(200, 200, 200), ambient(50, 50, 0),
                        specular(0, 0, 0), emissive(0, 0, 0), shininess(0) {}
};

struct PointLight {
    float x, y, z;
    float position[3];
    PointLight() : x(0.0f), y(0.0f), z(0.0f) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
    }
};

struct Light {
    std::string type;
    PointLight position;
    PointLight direction;
    float cutoff;


};





struct Transforms {
    // Color *color;
    Transform scale;
    Transform translate;
    Transform rotate;
    std::vector<string> order;
    bool align;

    Transforms() {
        //color = nullptr;
        scale.angle = 0;
        scale.time = 0;
        scale.x = 1;
        scale.y = 1;
        scale.z = 1;
        translate.x = 0;
        translate.y = 0;
        translate.z = 0;
        rotate.angle = 0;
        rotate.time = 0;
        rotate.x = 0;
        rotate.y = 0;
        rotate.z = 0;
        align = false;
        order = {};
    }
};

struct CatmullCurve {
    std::vector<Point> controlPoints;
};


struct Model {
    string file;
    Transform translate;
    int textureId;
    ColorProperties color;
    string textureFile;
    // Constructor to set default texture file
    Model() : textureFile("earth.jpg") {}

};


struct Group {
    Transforms transform;
    vector<Model> models;
    vector<Group> groups;
    std::vector<CatmullCurve> catmullCurves;
};

struct World {
    Camera camera;
    vector<Group> groups;
    map<string, vector<Point>> points;
    map<string, ModelData> modelData;
    vector<string> files;
    vector<Light> lights;
    std::vector<CatmullCurve> catmullCurves;
};
#endif //ENGINE_STRUCTS_H