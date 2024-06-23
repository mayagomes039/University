#include <vector>
#include <string>
#include <map>

using namespace std;

#ifndef ENGINE_STRUCTS_H
#define ENGINE_STRUCTS_H
struct Point {
    float x, y, z;
};

struct Camera {
    float position[3], lookAt[3], up[3], projection[3];
};

struct Transform {
    float angle, time, x, y, z;
    bool align;
    Transform() : angle(0),time(0), x(0), y(0), z(0), align(false) {}
};

struct Color {
    float x,y,z;
    Color(float x, float y, float z) : x(x), y(y), z(z) {}
};

struct Transforms {
    Color *color;
    Transform scale;
    Transform translate;
    Transform rotate;

    Transforms() {
        color = nullptr;
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
    }
};

struct CatmullCurve {
    std::vector<Point> controlPoints;
};

struct Model {
    string file;
    Transform translate;
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
    vector<string> files;
    std::vector<CatmullCurve> catmullCurves;
};
#endif //ENGINE_STRUCTS_H
