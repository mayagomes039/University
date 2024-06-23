#include <iostream>
#include <vector>
#include <string>
#include <fstream>
#include <sstream>
#include <cmath>
#include <unistd.h>

using namespace std;

struct Point {
    float x, y, z;
    float nx, ny, nz;
    float tx, tz;
};

vector<vector<int>> patches;
vector<Point> points;

float BezierMatrix[4][4] = {{-1, 3,  -3, 1},
                            {3, -6, 3, 0},
                            {-3, 3, 0, 0},
                            {1, 0, 0, 0}};

// Função para criar um plano
bool createPlane(float length, float divisions, const string& filename) {
    string res = "";

    if (length < 0 || divisions < 0) return false;

    int found = filename.find(".3d");
    if(found <= 0) return false;

    Point p1, p2, p3, p4;
    float z1, z2, x1, x2, tx1, tz1, tx2, tz2;
    float vertexStep = length/divisions;
    int texI = 0, texJ = 0;
    p1.y = p2.y = p3.y = p4.y = 0;

    for (float axeZ = -length/2; axeZ < length/2; axeZ+=vertexStep) {
        z1 = axeZ;        tz1 = texJ;
        z2 = (axeZ + vertexStep);  tz2 = (texJ+1);

        texI = 0;
        for (float axeX = -length/2; axeX < length/2; axeX+=vertexStep) {
            x1 = axeX;        tx1 = texI;
            x2 = (axeX + vertexStep);  tx2 = (texI+1);

            texI++;

            p1 = {x1, 0, z1, 0,1,0, tx1, tz1};
            p2 = {x2, 0, z1, 0,1,0, tx2, tz1};
            p3 = {x2, 0, z2, 0,1,0, tx2, tz2};
            p4 = {x1, 0, z2, 0,1,0, tx1, tz2};

            //p2
            res = res + to_string(p2.x) + ";" + to_string(p2.y) + ";" + to_string(p2.z) + ";" + // vertex
                  to_string(p2.nx) + ";" + to_string(p2.ny) + ";" + to_string(p2.nz) + ";" +  // normals
                  to_string(p2.tx) + ";" + to_string(p2.tz) + "\n";                               // texture
            //p1
            res = res + to_string(p1.x) + ";" + to_string(p1.y) + ";" + to_string(p1.z) + ";" +
                  to_string(p1.nx) + ";" + to_string(p1.ny) + ";" + to_string(p1.nz) + ";" +
                  to_string(p1.tx) + ";" + to_string(p1.tz) + "\n";
            //p4
            res = res + to_string(p4.x) + ";" + to_string(p4.y) + ";" + to_string(p4.z) + ";" +
                  to_string(p4.nx) + ";" + to_string(p4.ny) + ";" + to_string(p4.nz) + ";" +
                  to_string(p4.tx) + ";" + to_string(p4.tz) + "\n";
            //p4
            res = res + to_string(p4.x) + ";" + to_string(p4.y) + ";" + to_string(p4.z) + ";" +
                  to_string(p4.nx) + ";" + to_string(p4.ny) + ";" + to_string(p4.nz) + ";" +
                  to_string(p4.tx) + ";" + to_string(p4.tz) + "\n";
            //p3
            res = res + to_string(p3.x) + ";" + to_string(p3.y) + ";" + to_string(p3.z) + ";" +
                  to_string(p3.nx) + ";" + to_string(p3.ny) + ";" + to_string(p3.nz) + ";" +
                  to_string(p3.tx) + ";" + to_string(p3.tz) + "\n";
            //p2
            res = res + to_string(p2.x) + ";" + to_string(p2.y) + ";" + to_string(p2.z) + ";" +
                  to_string(p2.nx) + ";" + to_string(p2.ny) + ";" + to_string(p2.nz) + ";" +
                  to_string(p2.tx) + ";" + to_string(p2.tz) + "\n";
        }
        texJ++;
    }
    //escrever no ficheiro
    char tmp[256];
    getcwd(tmp, 256);

    string path(tmp);
    int found2 = path.find("Generator");
    replace(path.begin(), path.end(), '\\', '/');
    path.erase(path.begin() + found2, path.end());
    string path3d = path + "Models/" + filename;
    ofstream MyFile(path3d);
    MyFile << res;
    MyFile.close();
    cout << "File " << filename << " written successfully\n";
}

// Função para criar uma caixa
bool createBox(int units, int grid, string file) {
    string res = "";

    if (units < 0 || grid < 0) return false;

    int found = file.find(".3d");
    if(found <= 0) return false;

    //Vetor de crescimento de triângulos
    const int vetores[3][2][3] = { {{ 1,0,0 },{ 0,1,0 }}, {{ 0,0,-1 },{ 0,1,0 }}, {{ 0,0,-1 },{ 1,0,0 }} };

    float length = static_cast<float>(units) / grid;
    float begin = static_cast<float>(units) / 2;
    float texStep = 1.0f / grid;

    float i1 = 0 - begin;
    float i2 = 0 - begin;
    float i3 = begin;


    //faz as faces inversas também
    for (int i = 0; i < 2; i++) {
        float a1 = i1;
        float a2 = i2;
        float a3 = i3;

        for (int j = 0; j < grid; j++) {
            //de modo a conseguir subir na grid, é necessário guardar o primeiro ponto da ultima grid
            float aa1 = a1;
            float aa2 = a2;
            float aa3 = a3;

            float ty1 = (float)j * texStep;
            float ty2 = (float)(j+1) * texStep;

            for (int k = 0; k < grid; k++) {

                float b1 = a1 + (vetores[i][0][0] * length);
                float b2 = a2 + (vetores[i][0][1] * length);
                float b3 = a3 + (vetores[i][0][2] * length);

                float c1 = a1 + (vetores[i][1][0] * length);
                float c2 = a2 + (vetores[i][1][1] * length);
                float c3 = a3 + (vetores[i][1][2] * length);

                float d1 = a1 + ((vetores[i][0][0] + vetores[i][1][0]) * length);
                float d2 = a2 + ((vetores[i][0][1] + vetores[i][1][1]) * length);
                float d3 = a3 + ((vetores[i][0][2] + vetores[i][1][2]) * length);

                float tx1 = (float)k * texStep;
                float tx2 = (float)(k+1) * texStep;

                if(i==0){
                    //Face frontal
                    res = res + to_string(a1) + ";" + to_string(a2) + ";" + to_string(a3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(1.0f) + ";" +
                          to_string(tx1) + ";" + to_string(ty1) + "\n" +
                          to_string(b1) + ";" + to_string(b2) + ";" + to_string(b3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(1.0f) + ";" +
                          to_string(tx2) + ";" + to_string(ty1) + "\n" +
                          to_string(c1) + ";" + to_string(c2) + ";" + to_string(c3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(1.0f) + ";" +
                          to_string(tx1) + ";" + to_string(ty2) + "\n";

                    res = res + to_string(b1) + ";" + to_string(b2) + ";" + to_string(b3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(1.0f) + ";" +
                          to_string(tx2) + ";" + to_string(ty1) + "\n" +
                          to_string(d1) + ";" + to_string(d2) + ";" + to_string(d3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(1.0f) + ";" +
                          to_string(tx2) + ";" + to_string(ty2) + "\n" +
                          to_string(c1) + ";" + to_string(c2) + ";" + to_string(c3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(1.0f) + ";" +
                          to_string(tx1) + ";" + to_string(ty2) + "\n";


                    //Face inversa
                    res = res + to_string(0-b1) + ";" + to_string(0-b2) + ";" + to_string(0-b3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(-1.0f) + ";" +
                          to_string(tx2) + ";" + to_string(1-ty1) + "\n" +
                          to_string(0-a1) + ";" + to_string(0-a2) + ";" + to_string(0-a3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(-1.0f) + ";" +
                          to_string(tx1) + ";" + to_string(1-ty1) + "\n" +
                          to_string(0-c1) + ";" + to_string(0-c2) + ";" + to_string(0-c3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(-1.0f) + ";" +
                          to_string(tx1) + ";" + to_string(1-ty2) + "\n";

                    res = res + to_string(0-d1) + ";" + to_string(0-d2) + ";" + to_string(0-d3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(-1.0f) + ";" +
                          to_string(tx2) + ";" + to_string(1-ty2) + "\n" +
                          to_string(0-b1) + ";" + to_string(0-b2) + ";" + to_string(0-b3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(-1.0f) + ";" +
                          to_string(tx2) + ";" + to_string(1-ty1) + "\n" +
                          to_string(0-c1) + ";" + to_string(0-c2) + ";" + to_string(0-c3) + ";" +
                          to_string(0.0f) + ";" + to_string(0.0f) + ";" + to_string(-1.0f) + ";" +
                          to_string(tx1) + ";" + to_string(1-ty2) + "\n";
                } else if (i==1){
                    //Face esquerda
                    res = res + to_string(b1) + ";" + to_string(b2) + ";" + to_string(b3) + ";" +
                          to_string(-1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx2) + ";" + to_string(ty1) + "\n" +
                          to_string(a1) + ";" + to_string(a2) + ";" + to_string(a3) + ";" +
                          to_string(-1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx1) + ";" + to_string(ty1) + "\n" +
                          to_string(c1) + ";" + to_string(c2) + ";" + to_string(c3) + ";" +
                          to_string(-1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx1) + ";" + to_string(ty2) + "\n";

                    res = res + to_string(d1) + ";" + to_string(d2) + ";" + to_string(d3) + ";" +
                          to_string(-1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx2) + ";" + to_string(ty2) + "\n" +
                          to_string(b1) + ";" + to_string(b2) + ";" + to_string(b3) + ";" +
                          to_string(-1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx2) + ";" + to_string(ty1) + "\n" +
                          to_string(c1) + ";" + to_string(c2) + ";" + to_string(c3) + ";" +
                          to_string(-1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx1) + ";" + to_string(ty2) + "\n";

                    //Face direita
                    res = res + to_string(0-a1) + ";" + to_string(0-a2) + ";" + to_string(0-a3) + ";" +
                          to_string(1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx1) + ";" + to_string(1-ty1) + "\n" +
                          to_string(0-b1) + ";" + to_string(0-b2) + ";" + to_string(0-b3) + ";" +
                          to_string(1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx2) + ";" + to_string(1-ty1) + "\n" +
                          to_string(0-c1) + ";" + to_string(0-c2) + ";" + to_string(0-c3) + ";" +
                          to_string(1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx1) + ";" + to_string(1-ty2) + "\n";

                    res = res + to_string(0-b1) + ";" + to_string(0-b2) + ";" + to_string(0-b3) + ";" +
                          to_string(1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx2) + ";" + to_string(1-ty1) + "\n" +
                          to_string(0-d1) + ";" + to_string(0-d2) + ";" + to_string(0-d3) + ";" +
                          to_string(1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx2) + ";" + to_string(1-ty2) + "\n" +
                          to_string(0-c1) + ";" + to_string(0-c2) + ";" +  to_string(0-c3) + ";" +
                          to_string(1.0f) + ";" + to_string(0.0f) + ";" + to_string(0.0f) + ";" +
                          to_string(1-tx1) + ";" + to_string(1-ty2) + "\n";
                }

                a1 = b1;
                a2 = b2;
                a3 = b3;
            }

            a1 = aa1 + (vetores[i][1][0] * length);
            a2 = aa2 + (vetores[i][1][1] * length);
            a3 = aa3 + (vetores[i][1][2] * length);

        }
    }

    i1 = 0 - begin;
    i2 = begin;
    i3 = begin;
    float a1 = i1;
    float a2 = i2;
    float a3 = i3;

    for (int i = 0; i < grid; i++) {
        float aa1 = a1;
        float aa2 = a2;
        float aa3 = a3;

        float ty1 = (float)i * texStep;
        float ty2 = (float)(i+1) * texStep;

        for (int j = 0; j < grid; j++) {
            float b1 = a1 + (vetores[2][0][0] * length);
            float b2 = a2 + (vetores[2][0][1] * length);
            float b3 = a3 + (vetores[2][0][2] * length);

            float c1 = a1 + (vetores[2][1][0] * length);
            float c2 = a2 + (vetores[2][1][1] * length);
            float c3 = a3 + (vetores[2][1][2] * length);


            float d1 = a1 + ((vetores[2][0][0] + vetores[2][1][0]) * length);
            float d2 = a2 + ((vetores[2][0][1] + vetores[2][1][1]) * length);
            float d3 = a3 + ((vetores[2][0][2] + vetores[2][1][2]) * length);

            float tx1 = (float)j * texStep;
            float tx2 = (float)(j+1) * texStep;

            //Face de cima
            res = res + to_string(c1) + ";" + to_string(c2) + ";" + to_string(c3) + ";" +
                  to_string(0.0f) + ";" + to_string(1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx1) + ";" + to_string(ty2) + "\n" +
                  to_string(b1) + ";" + to_string(b2) + ";" + to_string(b3) + ";" +
                  to_string(0.0f) + ";" + to_string(1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx2) + ";" + to_string(ty1) + "\n" +
                  to_string(a1) + ";" + to_string(a2) + ";" + to_string(a3) + ";" +
                  to_string(0.0f) + ";" + to_string(1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx1) + ";" + to_string(ty1) + "\n";

            res = res + to_string(b1) + ";" + to_string(b2) + ";" + to_string(b3) + ";" +
                  to_string(0.0f) + ";" + to_string(1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx2) + ";" + to_string(ty1) + "\n" +
                  to_string(c1) + ";" + to_string(c2) + ";" + to_string(c3) + ";" +
                  to_string(0.0f) + ";" + to_string(1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx1) + ";" + to_string(ty2) + "\n" +
                  to_string(d1) + ";" + to_string(d2) + ";" + to_string(d3) + ";" +
                  to_string(0.0f) + ";" + to_string(1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx2) + ";" + to_string(ty2) + "\n";

            //Face de baixo
            res = res + to_string(0-c1) + ";" + to_string(0-c2) + ";" + to_string(0-c3) + ";" +
                  to_string(0.0f) + ";" + to_string(-1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx1) + ";" + to_string(ty2) + "\n" +
                  to_string(0-a1) + ";" + to_string(0-a2) + ";" + to_string(0-a3) + ";" +
                  to_string(0.0f) + ";" + to_string(-1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx1) + ";" + to_string(ty1) + "\n" +
                  to_string(0-b1) + ";" + to_string(0-b2) + ";" + to_string(0-b3) + ";" +
                  to_string(0.0f) + ";" + to_string(-1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx2) + ";" + to_string(ty1) + "\n";

            res = res + to_string(0-b1) + ";" + to_string(0-b2) + ";" + to_string(0-b3) + ";" +
                  to_string(0.0f) + ";" + to_string(-1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx2) + ";" + to_string(ty1) + "\n" +
                  to_string(0-d1) + ";" + to_string(0-d2) + ";" + to_string(0-d3) + ";" +
                  to_string(0.0f) + ";" + to_string(-1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx2) + ";" + to_string(ty2) + "\n" +
                  to_string(0-c1) + ";" + to_string(0-c2) + ";" + to_string(0-c3) + ";" +
                  to_string(0.0f) + ";" + to_string(-1.0f) + ";" + to_string(0.0f) + ";" +
                  to_string(tx1) + ";" + to_string(ty2) + "\n";

            a1 = b1;
            a2 = b2;
            a3 = b3;
        }
        a1 = aa1 + (vetores[2][1][0] * length);
        a2 = aa2 + (vetores[2][1][1] * length);
        a3 = aa3 + (vetores[2][1][2] * length);

    }

    //escrever no ficheiro
    char tmp[256];
    getcwd(tmp, 256);

    string path(tmp);
    int found2 = path.find("Generator");
    replace(path.begin(), path.end(), '\\', '/');
    path.erase(path.begin() + found2, path.end());
    string path3d = path + "Models/" + file;
    ofstream MyFile(path3d);
    MyFile << res;
    MyFile.close();
    cout << "File " << file << " written successfully\n";
}


void normalize(Point *a) {
    float l = sqrt((*a).x * (*a).x + (*a).y * (*a).y  + (*a).z * (*a).z );
    (*a).x = (*a).x/l;
    (*a).y = (*a).y/l;
    (*a).z = (*a).z/l;
}

// Função para criar uma esfera
void createSphere(float radius, int slices, int stacks, const string& filename) {
    string res = "";

    if (radius < 0 || slices < 0 || stacks < 0) return;

    int found = filename.find(".3d");
    if (found <= 0) return;


    float alpha;
    float aStep = (2*M_PI)/slices;
    float beta;
    float bStep = M_PI/stacks;
    float h, texX, texY, nexTexX, nexTexY;

    Point p1, p2, p3, p4;

    for(float b=-(stacks/2); b<(stacks/2); b++, h++) {
        beta=(b*bStep);

        for(float a=0; a<slices; a++) {
            alpha=(aStep*a);

            texX=(a/slices);
            texY=(h/stacks);
            nexTexX=((a+1)/slices);
            nexTexY=((h+1)/stacks);

            p1 = {radius*cosf(beta)*sinf(alpha), radius*sinf(beta), radius*cosf(beta)*cosf(alpha)
                    ,0,0,0, texX, texY};
            p2 = {radius*cosf(beta)*sinf(aStep*(a+1)), p1.y, radius*cosf(beta)*cosf(aStep*(a+1)),
                  0,0,0, nexTexX, texY};
            p3 = {radius*cosf((b+1)*bStep)*sinf(aStep*(a+1)), radius*sinf((b+1)*bStep),
                  radius*cosf((b+1)*bStep)*cosf(aStep*(a+1)), 0,0,0, nexTexX, nexTexY};
            p4 = {radius*cosf((b+1)*bStep)*sinf(alpha), p3.y, radius*cosf((b+1)*bStep)*cosf(alpha),
                  0,0,0, texX, nexTexY};

            // Normals
            Point n1 = p1;
            Point n2 = p2;
            Point n3 = p3;
            Point n4 = p4;
            normalize( &n1 );
            normalize( &n2 );
            normalize( &n3 );
            normalize( &n4 );
            p1.nx = n1.x; p1.ny = n1.y; p1.nz = n1.z;
            p2.nx = n2.x; p2.ny = n2.y; p2.nz = n2.z;
            p3.nx = n3.x; p3.ny = n3.y; p3.nz = n3.z;
            p4.nx = n4.x; p4.ny = n4.y; p4.nz = n4.z;

            //p1
            res = res + to_string(p1.x) + ";" + to_string(p1.y) + ";" + to_string(p1.z) + ";" +
                  to_string(p1.nx) + ";" + to_string(p1.ny) + ";" + to_string(p1.nz) + ";" +
                  to_string(p1.tx) + ";" + to_string(p1.tz) + "\n";
            //p2
            res = res + to_string(p2.x) + ";" + to_string(p2.y) + ";" + to_string(p2.z) + ";" +
                  to_string(p2.nx) + ";" + to_string(p2.ny) + ";" + to_string(p2.nz) + ";" +
                  to_string(p2.tx) + ";" + to_string(p2.tz) + "\n";
            //p3
            res = res + to_string(p3.x) + ";" + to_string(p3.y) + ";" + to_string(p3.z) + ";" +
                  to_string(p3.nx) + ";" + to_string(p3.ny) + ";" + to_string(p3.nz) + ";" +
                  to_string(p3.tx) + ";" + to_string(p3.tz) + "\n";

            //p3
            res = res + to_string(p3.x) + ";" + to_string(p3.y) + ";" + to_string(p3.z) + ";" +
                  to_string(p3.nx) + ";" + to_string(p3.ny) + ";" + to_string(p3.nz) + ";" +
                  to_string(p3.tx) + ";" + to_string(p3.tz) + "\n";
            //p4
            res = res + to_string(p4.x) + ";" + to_string(p4.y) + ";" + to_string(p4.z) + ";" +
                  to_string(p4.nx) + ";" + to_string(p4.ny) + ";" + to_string(p4.nz) + ";" +
                  to_string(p4.tx) + ";" + to_string(p4.tz) + "\n";
            //p1
            res = res + to_string(p1.x) + ";" + to_string(p1.y) + ";" + to_string(p1.z) + ";" +
                  to_string(p1.nx) + ";" + to_string(p1.ny) + ";" + to_string(p1.nz) + ";" +
                  to_string(p1.tx) + ";" + to_string(p1.tz) + "\n";
        }
    }
    //escrever no ficheiro
    char tmp[256];
    getcwd(tmp, 256);

    string path(tmp);
    int found2 = path.find("Generator");
    replace(path.begin(), path.end(), '\\', '/');
    path.erase(path.begin() + found2, path.end());
    string path3d = path + "Models/" + filename;
    ofstream MyFile(path3d);
    MyFile << res;
    MyFile.close();
    cout << "File " << filename << " written successfully\n";

}


// Função para criar um cone
void createCone(float radius, float height, int slices, int stacks, const string& filename) {
    string res = "";

    if (radius < 0 || height < 0 || slices < 0 || stacks < 0) return;

    int found = filename.find(".3d");
    if(found <= 0) return;

    //Calcute height of each stack
    float stackHeight = height / stacks;

    float angle = (2 * M_PI) / slices;
    Point p1, p2, p3, p4;

    //Circunference of points, given slices height and radius
    for (float i = 0; i < stacks; i++) {
        float stackRadius = ((radius * (height - (stackHeight * i))) / height);
        float stackRadius2 = ((radius * (height - (stackHeight * (i + 1)))) / height);

        for (int c = 0; c < slices; c++) {
            // Sides
            float alpha = angle * c;
            float alpha2 = angle * (c + 1);

            float p1x = cos(alpha) * stackRadius;
            float p1y = stackHeight * i;
            float p1z = -sin(alpha) * stackRadius;
            float nx1 = stackRadius*sinf(alpha);
            float ny1 = sinf(atan(radius/height));
            float nz1 = stackRadius*cosf(alpha);

            float p2x = cos(alpha2) * stackRadius;
            float p2y = stackHeight * i;
            float p2z = -sin(alpha2) * stackRadius;
            float nx2 = stackRadius*sinf(alpha2);
            float ny2 = sinf(atan(radius/height));
            float nz2 = stackRadius*cosf(alpha2);

            float p3x = cos(alpha) * stackRadius2;
            float p3y = stackHeight * (i + 1);
            float p3z = -sin(alpha) * stackRadius2;
            float nx3 = stackRadius2*sinf(alpha2);
            float ny3 = sinf(atan(radius/height));
            float nz3 = stackRadius2*cosf(alpha2);

            float p4x = cos(alpha2) * stackRadius2;
            float p4y = stackHeight * (i + 1);
            float p4z = -sin(alpha2) * stackRadius2;
            float nx4 = stackRadius2*sinf(alpha);
            float ny4 = sinf(atan(radius/height));
            float nz4 = stackRadius2*cosf(alpha);

            float tx1 = (i/stacks)*cosf(alpha)+0.5;
            float ty1 = (i/stacks)*sinf(alpha)+0.5;
            float tx2 = (i/stacks)*cosf(alpha2)+0.5;
            float ty2 = (i/stacks)*sinf(alpha2)+0.5;
            float tx3 = ((i+1)/stacks)*cosf(alpha2)+0.5;
            float ty3 = ((i+1)/stacks)*sinf(alpha2)+0.5;
            float tx4 = ((i+1)/stacks)*cosf(alpha)+0.5;
            float ty4 = ((i+1)/stacks)*sinf(alpha)+0.5;

            p1 ={cos(alpha) * stackRadius, stackHeight * i, -sin(alpha) * stackRadius, nx1, ny1, nz1, tx1, ty1};
            p2 ={cos(alpha2) * stackRadius, stackHeight * i, -sin(alpha2) * stackRadius, nx2, ny2, nz2, tx2, ty2};
            p3 ={cos(alpha) * stackRadius2, stackHeight * (i + 1), -sin(alpha) * stackRadius2, nx3, ny3, nz3, tx3, ty3};
            p4 ={cos(alpha2) * stackRadius2, stackHeight * (i + 1), -sin(alpha2) * stackRadius2, nx4, ny4, nz4, tx4, ty4};

            if (i == 0) {
                res = res + to_string(p3.x) + ";" + to_string(p3.y) + ";" + to_string(p3.z) + ";" +
                      to_string(p3.nx) + ";" + to_string(p3.ny) + ";" + to_string(p3.nz) + ";" +
                      to_string(p3.tx) + ";" + to_string(p3.tz) + "\n" +
                      to_string(p1.x) + ";" + to_string(p1.y) + ";" + to_string(p1.z) + ";" +
                      to_string(p1.nx) + ";" + to_string(p1.ny) + ";" + to_string(p1.nz) + ";" +
                      to_string(p1.tx) + ";" + to_string(p1.tz) + "\n" +
                      to_string(p2.x) + ";" + to_string(p2.y) + ";" + to_string(p2.z) + ";" +
                      to_string(p2.nx) + ";" + to_string(p2.ny) + ";" + to_string(p2.nz) + ";" +
                      to_string(p2.tx) + ";" + to_string(p2.tz) + "\n";

                res = res + to_string(p3.x) + ";" + to_string(p3.y) + ";" + to_string(p3.z) + ";" +
                      to_string(p3.nx) + ";" + to_string(p3.ny) + ";" + to_string(p3.nz) + ";" +
                      to_string(p3.tx) + ";" + to_string(p3.tz) + "\n" +
                      to_string(p2.x) + ";" + to_string(p2.y) + ";" + to_string(p2.z) + ";" +
                      to_string(p2.nx) + ";" + to_string(p2.ny) + ";" + to_string(p2.nz) + ";" +
                      to_string(p2.tx) + ";" + to_string(p2.tz) + "\n" +
                      to_string(p4.x) + ";" + to_string(p4.y) + ";" + to_string(p4.z) + ";" +
                      to_string(p4.nx) + ";" + to_string(p4.ny) + ";" + to_string(p4.nz) + ";" +
                      to_string(p4.tx) + ";" + to_string(p4.tz) + "\n";

                // Base
                res = res + to_string(0.000000) + ";" + to_string(0.000000) + ";" + to_string(0.000000) + ";" +
                      to_string(0.0f) + ";" + to_string(-1.0f) + ";" + to_string(0.0f) + ";" +
                      to_string(0.5f) + ";" + to_string(0.5f) + "\n" +
                      to_string(p2.x) + ";" + to_string(p2.y) + ";" + to_string(p2.z) + ";" +
                      to_string(0.0f) + ";" + to_string(-1.0f) + ";" + to_string(0.0f) + ";" +
                      to_string(cosf(alpha)*0.5+0.5) + ";" + to_string(sinf(alpha)*0.5+0.5) + "\n" +
                      to_string(p1.x) + ";" + to_string(p1.y) + ";" + to_string(p1.z) +
                      to_string(0.0f) + ";" + to_string(-1.0f) + ";" + to_string(0.0f) + ";" +
                      to_string(cosf(alpha2)*0.5+0.5) + ";" + to_string(sinf(alpha2)*0.5+0.5) + "\n";
            }
            else if (i != (stacks - 1)) {
                res = res + to_string(p3.x) + ";" + to_string(p3.y) + ";" + to_string(p3.z) + ";" +
                      to_string(p3.nx) + ";" + to_string(p3.ny) + ";" + to_string(p3.nz) + ";" +
                      to_string(p3.tx) + ";" + to_string(p3.tz) + "\n" +
                      to_string(p1.x) + ";" + to_string(p1.y) + ";" + to_string(p1.z) + ";" +
                      to_string(p1.nx) + ";" + to_string(p1.ny) + ";" + to_string(p1.nz) + ";" +
                      to_string(p1.tx) + ";" + to_string(p1.tz) + "\n" +
                      to_string(p2.x) + ";" + to_string(p2.y) + ";" + to_string(p2.z) + ";" +
                      to_string(p2.nx) + ";" + to_string(p2.ny) + ";" + to_string(p2.nz) + ";" +
                      to_string(p2.tx) + ";" + to_string(p2.tz) + "\n";

                res = res + to_string(p3.x) + ";" + to_string(p3.y) + ";" + to_string(p3.z) + ";" +
                      to_string(p3.nx) + ";" + to_string(p3.ny) + ";" + to_string(p3.nz) + ";" +
                      to_string(p3.tx) + ";" + to_string(p3.tz) + "\n" +
                      to_string(p2.x) + ";" + to_string(p2.y) + ";" + to_string(p2.z) + ";" +
                      to_string(p2.nx) + ";" + to_string(p2.ny) + ";" + to_string(p2.nz) + ";" +
                      to_string(p2.tx) + ";" + to_string(p2.tz) + "\n" +
                      to_string(p4.x) + ";" + to_string(p4.y) + ";" + to_string(p4.z) + ";" +
                      to_string(p4.nx) + ";" + to_string(p4.ny) + ";" + to_string(p4.nz) + ";" +
                      to_string(p4.tx) + ";" + to_string(p4.tz) + "\n";
            }
            else {
                res = res + to_string(p3.x) + ";" + to_string(p3.y) + ";" + to_string(p3.z) + ";" +
                      to_string(p3.nx) + ";" + to_string(p3.ny) + ";" + to_string(p3.nz) + ";" +
                      to_string(p3.tx) + ";" + to_string(p3.tz) + "\n" +
                      to_string(p1.x) + ";" + to_string(p1.y) + ";" + to_string(p1.z) + ";" +
                      to_string(p1.nx) + ";" + to_string(p1.ny) + ";" + to_string(p1.nz) + ";" +
                      to_string(p1.tx) + ";" + to_string(p1.tz) + "\n" +
                      to_string(p2.x) + ";" + to_string(p2.y) + ";" + to_string(p2.z) + ";" +
                      to_string(p2.nx) + ";" + to_string(p2.ny) + ";" + to_string(p2.nz) + ";" +
                      to_string(p2.tx) + ";" + to_string(p2.tz) + "\n";
            }
        }
    }
    //escrever no ficheiro
    char tmp[256];
    getcwd(tmp, 256);

    string path(tmp);
    int found2 = path.find("Generator");
    replace(path.begin(), path.end(), '\\', '/');
    path.erase(path.begin() + found2, path.end());
    string path3d = path + "Models/" + filename;
    ofstream MyFile(path3d);
    MyFile << res;
    MyFile.close();
    cout << "File " << filename << " written successfully\n";
}

//Função para criar um anel
void createRing(float radius, float outradius, float slices, const string& filename) {
    string modelsFolder = "../Models/";
    string filePath = modelsFolder + filename;

    ofstream outfile(filePath);
    if (!outfile.is_open()) {
        cerr << "Erro ao abrir o arquivo." << endl;
        return;
    }

    float inradius = outradius - radius;
    float aStep = (2 * M_PI) / slices;
    float alpha;
    Point pt1, pt2, pt3, pt4;

    for (float a = 0; a < 2 * slices; a++) {
        alpha = (aStep * a);
        float x1, x2, x3, x4, z1, z2, z3, z4;
        x1 = outradius * sinf(alpha);
        x2 = outradius * sinf(aStep * (a + 1));
        x3 = inradius * sinf(aStep * (a + 1));
        x4 = inradius * sinf(alpha);
        z1 = outradius * cosf(alpha);
        z2 = outradius * cosf(aStep * (a + 1));
        z3 = inradius * cosf(aStep * (a + 1));
        z4 = inradius * cosf(alpha);

        pt1 = {x1, 0, z1, 0, -1, 0, 0, 0};
        pt2 = {x2, 0, z2, 0, -1, 0, 0, 0};
        pt3 = {x3, 0, z3, 0, -1, 0, 0, 0};
        pt4 = {x4, 0, z4, 0, -1, 0, 0, 0};

        outfile << pt1.x << ";" << pt1.y << ";" << pt1.z << "/"
                << pt2.x << ";" << pt2.y << ";" << pt2.z << "/"
                << pt4.x << ";" << pt4.y << ";" << pt4.z << "\n";

        outfile << pt2.x << ";" << pt2.y << ";" << pt2.z << "/"
                << pt3.x << ";" << pt3.y << ";" << pt3.z << "/"
                << pt4.x << ";" << pt4.y << ";" << pt4.z << "\n";

        // Gerar os pontos inferiores invertendo a ordem
        pt1.y = pt2.y = pt3.y = pt4.y = -pt1.y;

        outfile << pt2.x << ";" << pt2.y << ";" << pt2.z << "/"
                << pt1.x << ";" << pt1.y << ";" << pt1.z << "/"
                << pt4.x << ";" << pt4.y << ";" << pt4.z << "\n";

        outfile << pt4.x << ";" << pt4.y << ";" << pt4.z << "/"
                << pt3.x << ";" << pt3.y << ";" << pt3.z << "/"
                << pt2.x << ";" << pt2.y << ";" << pt2.z << "\n";
    }

    outfile.close();
}

bool readFile(string file) {
    string line, number;
    int i = 0, numberPatch = 0;

    string pathPatch = "../Models/" + file;

    ifstream infile(pathPatch, ios::binary | ios::in);

    if (!infile) {
        cout << "Error opening patch file!\n";
        return false;
    }

    while (getline(infile, line, '\n')) {
        line.erase(std::remove(line.begin(), line.end(), '\r'), line.end());

        if (i==0){
            numberPatch = stoi(line);
        }
        else if (i <= numberPatch){
            istringstream iss(line);
            vector<int> patch;
            while(getline(iss, number, ',')){
                patch.push_back(stoi(number));
            }
            patches.push_back(patch);
        }
        else if(i > numberPatch + 1){
            istringstream iss(line);
            int j = 0;
            float p[3];

            while (getline(iss, number, ',')){
                p[j] = stof(number);
                j++;
            }
            points.push_back({p[0], p[1], p[2]});
        }
        i++;
    }
    infile.close();
    return true;
}


Point multMatrixVector(float u[4], vector<vector<Point>> matrix, float v[4]){
    vector<Point> aux(4);
    for (int i = 0; i < 4; ++i) {
        aux[i] = {0,0,0};
    }

    for (int j = 0; j < 4; ++j) {
        for (int k = 0; k < 4; ++k) {

            Point p = matrix[k][j];
            float x = p.x;
            float y = p.y;
            float z = p.z;
            p = {x*u[k], y*u[k], z*u[k]};

            Point p_aux = aux[j];
            aux[j] = {p.x+p_aux.x, p.y+p_aux.y, p.z+p_aux.z};
        }
    }

    Point final = {0,0,0};

    for(int j =0; j<4;++j){
        final.x = final.x + (aux[j].x * v[j]);
        final.y = final.y + (aux[j].y * v[j]);
        final.z = final.z + (aux[j].z * v[j]);
    }
    return final;
}

vector<vector<Point>> matrixMultiplication(Point p[4][4], float m[4][4]){
    vector<vector<Point>> res(4,vector<Point>(4));
    for (int i = 0; i < 4; ++i) {
        for (int j = 0; j < 4; ++j) {
            res[i][j] = {0,0,0, 0, 0, 0, 0, 0};
        }
    }

    for (int i = 0; i < 4; ++i) {
        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 4; ++k) {
                Point point = p[k][j];
                float x = point.x;
                float y = point.y;
                float z = point.z;
                point = {x*m[i][k], y*m[i][k], z*m[i][k]};

                Point aux = res[i][j];
                res[i][j] = {point.x + aux.x, point.y + aux.y, point.z + aux.z};
            }
        }
    }

    vector<vector<Point>> result(4,vector<Point>(4));
    for (int i = 0; i < 4; ++i) {
        for (int j = 0; j < 4; ++j) {
            result[i][j] = {0,0,0, 0,0,0,0,0};
        }
    }

    for (int i = 0; i < 4; ++i) {
        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 4; ++k) {
                Point point = res[i][k];
                float x = point.x;
                float y = point.y;
                float z = point.z;
                point = {x*m[k][j], y*m[k][j], z*m[k][j]};

                Point aux = result[i][j];
                result[i][j] = {point.x + aux.x, point.y + aux.y, point.z + aux.z};
            }
        }
    }
    return result;
}

Point cross(Point a, Point b) {
    Point res = {
            a.y * b.z - a.z * b.y,
            a.z * b.x - a.x * b.z,
            a.x * b.y - a.y * b.x
    };
    return res;
}

string calcBezier(Point p[4][4], int tessellation){
    float pu=0, pv=0;
    vector<vector<Point>> grid(tessellation+1, vector<Point>(tessellation+1));
    vector<vector<Point>> gridNorm(tessellation+1, vector<Point>(tessellation+1));
    vector<vector<Point>> gridText(tessellation+1, vector<Point>(tessellation+1));

    for (int j = 0; j <= tessellation; j++) {
        for (int k = 0; k <= tessellation; k++) {
            grid[j][k] = {0,0,0, 0, 0, 0, 0, 0};
            gridNorm[j][k] = {0,0,0, 0, 0, 0, 0, 0};
            gridText[j][k] = {0,0,0, 0, 0, 0, 0, 0};
        }
    }
    float m[4][4] = {{-1, 3,  -3, 1},
                     {3, -6, 3, 0},
                     {-3, 3, 0, 0},
                     {1, 0, 0, 0}};
    string res;

    vector<vector<Point>> matrix = matrixMultiplication(p, m);

    for (int i = 0; i <= tessellation; i++) {
        pu = (float)i / (float)tessellation;
        float u_vector[4] = {pu*pu*pu, pu*pu,pu,1};
        float u_deriv[4] = {3*pu*pu, 2*pu, 1, 0};

        for (int j = 0; j <= tessellation; j++) {
            pv = (float)j / (float)tessellation;
            float v_vector[4] = {pv*pv*pv, pv*pv,pv,1};
            float v_deriv[4] = {3*pv*pv, 2*pv, 1, 0};

            grid[i][j] = multMatrixVector(u_vector, matrix, v_vector);
            Point tanV = multMatrixVector(u_vector, matrix, v_deriv);
            Point tanU = multMatrixVector(u_deriv, matrix, v_vector);
            Point norm = cross(tanV, tanU);
            normalize(&norm);

            gridNorm[i][j].nx = norm.x;
            gridNorm[i][j].ny = norm.y;
            gridNorm[i][j].nz = norm.z;

            gridText[i][j].tx = pu;
            gridText[i][j].tz = pv;
        }
    }

    for (int j = 0; j < tessellation; j++) {
        for (int k = 0; k < tessellation; k++) {

            res += to_string(grid[j][k].x) + ";" + to_string(grid[j][k].y) + ";" + to_string(grid[j][k].z) + ";" +
                   to_string(gridNorm[j][k].nx) + ";" + to_string(gridNorm[j][k].ny) + ";" + to_string(gridNorm[j][k].nz) + ";" +
                   to_string(gridText[j][k].tx) + ";" + to_string(gridText[j][k].tz) + "\n" +
                   to_string(grid[j+1][k].x) + ";" + to_string(grid[j+1][k].y) + ";" + to_string(grid[j+1][k].z) + ";" +
                   to_string(gridNorm[j+1][k].nx) + ";" + to_string(gridNorm[j+1][k].ny) + ";" + to_string(gridNorm[j+1][k].nz) + ";" +
                   to_string(gridText[j+1][k].tx) + ";" + to_string(gridText[j+1][k].tz) + "\n" +
                   to_string(grid[j][k+1].x) + ";" + to_string(grid[j][k+1].y) + ";" + to_string(grid[j][k+1].z) + ";" +
                   to_string(gridNorm[j][k+1].nx) + ";" + to_string(gridNorm[j][k+1].ny) + ";" + to_string(gridNorm[j][k+1].nz) + ";" +
                   to_string(gridText[j][k+1].tx) + ";" + to_string(gridText[j][k+1].tz) + "\n";

            res +=  to_string(grid[j+1][k].x) + ";" + to_string(grid[j+1][k].y) + ";" + to_string(grid[j+1][k].z) + ";" +
                    to_string(gridNorm[j+1][k].nx) + ";" + to_string(gridNorm[j+1][k].ny) + ";" + to_string(gridNorm[j+1][k].nz) + ";" +
                    to_string(gridText[j+1][k].tx) + ";" + to_string(gridText[j+1][k].tz) + "\n" +
                    to_string(grid[j+1][k+1].x) + ";" + to_string(grid[j+1][k+1].y) + ";" + to_string(grid[j+1][k+1].z) + ";" +
                    to_string(gridNorm[j+1][k+1].nx) + ";" + to_string(gridNorm[j+1][k+1].ny) + ";" + to_string(gridNorm[j+1][k+1].nz) + ";" +
                    to_string(gridText[j+1][k+1].tx) + ";" + to_string(gridText[j+1][k+1].tz) + "\n" +
                    to_string(grid[j][k+1].x) + ";" + to_string(grid[j][k+1].y) + ";" + to_string(grid[j][k+1].z) + ";" +
                    to_string(gridNorm[j][k+1].nx) + ";" + to_string(gridNorm[j][k+1].ny) + ";" + to_string(gridNorm[j][k+1].nz) + ";" +
                    to_string(gridText[j][k+1].tx) + ";" + to_string(gridText[j][k+1].tz) + "\n";
        }
    }
    return res;
}

bool bezier(string inFile, int tessellation, string outFile){
    string res;
    if (tessellation < 0) return false;

    if (!readFile(inFile)) return false;

    Point m[4][4] = {0};
    for (int i = 0; i < patches.size(); i++) {
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                m[row][column] = points[patches[i][row + column * 4]];
            }
        }

        res += calcBezier(m, tessellation);
    }

    //escrever no ficheiro
    char tmp[256];
    getcwd(tmp, 256);

    string path(tmp);
    int found2 = path.find("Generator");
    replace(path.begin(), path.end(), '\\', '/');
    path.erase(path.begin() + found2, path.end());
    string path3d = path + "Models/" + outFile;
    ofstream MyFile(path3d);
    MyFile << res;
    MyFile.close();
    cout << "File " << outFile << " written successfully\n";
    return true;
}


int main(int argc, char* argv[]) {
    if (argc < 3 || argc > 7) {
        cout << "Usage: " << argv[0] << " <shape> <shape_parameters> <output_file> [additional_parameters]" << endl;
        return 1;
    }

    string shape = argv[1];
    string filename = argv[argc - 1]; // Output file name

    if (shape == "plane" || shape == "box" || shape == "sphere" || shape == "cone" || shape == "ring") {
        // Parse shape parameters based on shape type
        //if (argc != 5) {
        //cout << "Incorrect number of arguments for creating " << shape << "." << endl;
        //return 1;
        //}
        float length_or_radius = stof(argv[2]);
        int divisions_or_slices = stoi(argv[3]);

        if (shape == "plane") {
            createPlane(length_or_radius, divisions_or_slices, filename);
        } else if (shape == "box") {
            createBox(length_or_radius, divisions_or_slices, filename);
        } else if (shape == "sphere") {
            float radius = length_or_radius;
            int slices = divisions_or_slices;
            int stacks = stoi(argv[4]);
            createSphere(radius, slices, stacks, filename);
        } else if (shape == "cone") {
            float radius = length_or_radius;
            float height = stof(argv[3]);
            int slices = divisions_or_slices;
            int stacks = stoi(argv[4]);
            createCone(radius, height, slices, stacks, filename);
        } else if (shape == "ring") {
            float inner_radius = length_or_radius;
            float outer_radius = divisions_or_slices;
            float ring_slices = stof(argv[4]);
            createRing(inner_radius, outer_radius, ring_slices, filename);
        }
    } else if (shape == "bezier") {
        if (argc != 5) {
            cout << "Incorrect number of arguments for creating a bezier shape." << endl;
            return 1;
        }
        string bezierFile = argv[2];
        int tessellation = stoi(argv[3]);

        if (!bezier(bezierFile, tessellation, filename)) {
            cout << "Failed to generate Bezier shape." << endl;
            return 1;
        }
    } else {
        cout << "Invalid shape format. Use 'plane', 'box', 'sphere', 'cone', 'ring', or 'bezier'." << endl;
        return 1;
    }
    return 0;
}

