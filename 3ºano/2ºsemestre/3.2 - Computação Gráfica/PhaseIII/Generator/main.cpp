#include <iostream>
#include <vector>
#include <string>
#include <fstream>
#include <sstream>
#include <cmath> // Para funções matemáticas como sin, cos, etc.
#include <iomanip>

using namespace std;

// Definição da estrutura Point
struct Point {
    float x;
    float y;
    float z;
};

// Bezier
vector<vector<int>> patches;
vector<Point> points;

float BezierMatrix[4][4] = {{-1, 3,  -3, 1},
                            {3, -6, 3, 0},
                            {-3, 3, 0, 0},
                            {1, 0, 0, 0}};

// Função para criar um plano
void createPlane(float length, int divisions, const string& filename) {
    string modelsFolder = "../Models/";
    string filePath = modelsFolder + filename;

    ofstream outfile(filePath);
    if (!outfile.is_open()) {
        cerr << "Erro ao abrir o arquivo." << endl;
        return;
    }

    float dx = length / divisions;

    for (int i = 0; i < divisions; ++i) {
        for (int j = 0; j < divisions; ++j) {
            Point p1 = {-length / 2.0f + i * dx, 0.0f, -length / 2.0f + j * dx};
            Point p2 = {p1.x + dx, 0.0f, p1.z};
            Point p3 = {p2.x, 0.0f, p2.z + dx};
            Point p4 = {p1.x, 0.0f, p1.z + dx};

            // Escrever coordenadas no ficheiro
            outfile << p3.x << ";" << p3.y << ";" << p3.z << "/"
            << p2.x << ";" << p2.y << ";" << p2.z << "/"
            << p1.x << ";" << p1.y << ";" << p1.z << "\n"
        
            << p1.x << ";" << p1.y << ";" << p1.z << "/"
            << p4.x << ";" << p4.y << ";" << p4.z << "/"
            << p3.x << ";" << p3.y << ";" << p3.z << "\n";
        }
    }

    outfile.close();
}

// Função para criar uma caixa
void createBox(float length, int divisions, const string& filename) {
    string modelsFolder = "../Models/";
    string filePath = modelsFolder + filename;

    ofstream outfile(filePath);
    if (!outfile.is_open()) {
        cerr << "Erro ao abrir o arquivo." << endl;
        return;
    }

    float dx = length / divisions;

    // Bottom
    for (int i = 0; i < divisions; ++i) {
        for (int k = 0; k < divisions; ++k) {
            Point p1 = {-length / 2.0f + i * dx, -length / 2.0f, -length / 2.0f + k * dx};
            Point p2 = {p1.x + dx, p1.y, p1.z};
            Point p5 = {p1.x, p1.y, p1.z + dx};
            Point p6 = {p2.x, p2.y, p2.z + dx};

            outfile << p1.x << ";" << p1.y << ";" << p1.z << "/"
                    << p2.x << ";" << p2.y << ";" << p2.z << "/"
                    << p5.x << ";" << p5.y << ";" << p5.z << "\n"

                    << p5.x << ";" << p5.y << ";" << p5.z << "/"
                    << p2.x << ";" << p2.y << ";" << p2.z << "/"
                    << p6.x << ";" << p6.y << ";" << p6.z << "\n";
        }
    }

    // Top
    for (int i = 0; i < divisions; ++i) {
        for (int k = 0; k < divisions; ++k) {
            Point p1 = {-length / 2.0f + i * dx, length / 2.0f, -length / 2.0f + k * dx};
            Point p2 = {p1.x + dx, p1.y, p1.z};
            Point p5 = {p1.x, p1.y, p1.z + dx};
            Point p6 = {p2.x, p2.y, p2.z + dx};

            outfile << p5.x << ";" << p5.y << ";" << p5.z << "/"
                    << p2.x << ";" << p2.y << ";" << p2.z << "/"
                    << p1.x << ";" << p1.y << ";" << p1.z << "\n"

                    << p6.x << ";" << p6.y << ";" << p6.z << "/"
                    << p2.x << ";" << p2.y << ";" << p2.z << "/"
                    << p5.x << ";" << p5.y << ";" << p5.z << "\n";
        }
    }

    // Back
    for (int i = 0; i < divisions; ++i) {
        for (int k = 0; k < divisions; ++k) {
            Point p5 = {-length / 2.0f + k * dx, -length / 2.0f + i * dx, -length / 2.0f};
            Point p6 = {p5.x + dx, p5.y, p5.z};
            Point p7 = {p6.x, p6.y + dx, p6.z};
            Point p8 = {p5.x, p5.y + dx, p5.z};

            outfile << p8.x << ";" << p8.y << ";" << p8.z << "/"
                    << p6.x << ";" << p6.y << ";" << p6.z << "/"
                    << p5.x << ";" << p5.y << ";" << p5.z << "\n"

                    << p7.x << ";" << p7.y << ";" << p7.z << "/"
                    << p6.x << ";" << p6.y << ";" << p6.z << "/"
                    << p8.x << ";" << p8.y << ";" << p8.z << "\n";
        }
    }

    // Front
    for (int i = 0; i < divisions; ++i) {
        for (int k = 0; k < divisions; ++k) {
            Point p5 = {-length / 2.0f + k * dx, -length / 2.0f + i * dx, length / 2.0f};
            Point p6 = {p5.x + dx, p5.y, p5.z};
            Point p7 = {p6.x, p6.y + dx, p6.z};
            Point p8 = {p5.x, p5.y + dx, p5.z};

            outfile << p5.x << ";" << p5.y << ";" << p5.z << "/"
                    << p6.x << ";" << p6.y << ";" << p6.z << "/"
                    << p8.x << ";" << p8.y << ";" << p8.z << "\n"

                    << p7.x << ";" << p7.y << ";" << p7.z << "/"
                    << p8.x << ";" << p8.y << ";" << p8.z << "/"
                    << p6.x << ";" << p6.y << ";" << p6.z << "\n";
        }
    }

    // Right
    for (int i = 0; i < divisions; ++i) {
        for (int k = 0; k < divisions; ++k) {
            Point p5 = {-length / 2.0f, -length / 2.0f + i * dx, length / 2.0f - k * dx - dx};
            Point p1 = {p5.x, p5.y, p5.z + dx};
            Point p4 = {p1.x, p1.y + dx, p1.z};
            Point p8 = {p5.x, p5.y + dx, p5.z};

            outfile << p5.x << ";" << p5.y << ";" << p5.z << "/"
                    << p1.x << ";" << p1.y << ";" << p1.z << "/"
                    << p8.x << ";" << p8.y << ";" << p8.z << "\n"

                    << p4.x << ";" << p4.y << ";" << p4.z << "/"
                    << p8.x << ";" << p8.y << ";" << p8.z << "/"
                    << p1.x << ";" << p1.y << ";" << p1.z << "\n";
        }
    }

    // Left
    for (int i = 0; i < divisions; ++i) {
        for (int k = 0; k < divisions; ++k) {
            Point p5 = {length / 2.0f, -length / 2.0f + i * dx, length / 2.0f - k * dx - dx};
            Point p1 = {p5.x, p5.y, p5.z + dx};
            Point p4 = {p1.x, p1.y + dx, p1.z};
            Point p8 = {p5.x, p5.y + dx, p5.z};

            outfile << p5.x << ";" << p5.y << ";" << p5.z << "/"
                    << p8.x << ";" << p8.y << ";" << p8.z << "/"
                    << p1.x << ";" << p1.y << ";" << p1.z << "\n"

                    << p4.x << ";" << p4.y << ";" << p4.z << "/"
                    << p1.x << ";" << p1.y << ";" << p1.z << "/"
                    << p8.x << ";" << p8.y << ";" << p8.z << "\n";
        }
    }

    outfile.close();
}

// Função para criar uma esfera
void createSphere(float radius, int slices, int stacks, const string& filename) {
    string modelsFolder = "../Models/";
    string filePath = modelsFolder + filename;

    ofstream outfile(filePath);
    if (!outfile.is_open()) {
        cerr << "Erro ao abrir o arquivo." << endl;
        return;
    }

    if (radius < 0 || slices < 0 || stacks < 0) {
        cerr << "Parâmetros inválidos." << endl;
        return;
    }

    float alpha;
    float aStep = (2 * M_PI) / slices;
    float beta;
    float bStep = M_PI / stacks;

    Point p1, p2, p3, p4;

    for (int b = -(stacks / 2); b < (stacks / 2); b++) {
        beta = (b * bStep);

        for (int a = 0; a < slices; a++) {
            alpha = (aStep * a);

            p1.x = radius * cosf(beta) * sinf(alpha);
            p1.y = radius * sinf(beta);
            p1.z = radius * cosf(beta) * cosf(alpha);

            p2.x = radius * cosf(beta) * sinf(aStep * (a + 1));
            p2.y = p1.y;
            p2.z = radius * cosf(beta) * cosf(aStep * (a + 1));

            p3.x = radius * cosf((b + 1) * bStep) * sinf(aStep * (a + 1));
            p3.y = radius * sinf((b + 1) * bStep);
            p3.z = radius * cosf((b + 1) * bStep) * cosf(aStep * (a + 1));

            p4.x = radius * cosf((b + 1) * bStep) * sinf(alpha);
            p4.y = p3.y;
            p4.z = radius * cosf((b + 1) * bStep) * cosf(alpha);

            // Escrever coordenadas no ficheiro
            outfile << p1.x << ";" << p1.y << ";" << p1.z << "/"
                    << p2.x << ";" << p2.y << ";" << p2.z << "/"
                    << p3.x << ";" << p3.y << ";" << p3.z << "\n";

            outfile << p3.x << ";" << p3.y << ";" << p3.z << "/"
                    << p4.x << ";" << p4.y << ";" << p4.z << "/"
                    << p1.x << ";" << p1.y << ";" << p1.z << "\n";
        }
    }

    outfile.close();
}

// Função para criar um cone
void createCone(float radius, float height, int slices, int stacks, const string& filename) {
    string modelsFolder = "../Models/";
    string filePath = modelsFolder + filename;

    ofstream outfile(filePath);
    if (!outfile.is_open()) {
        cerr << "Erro ao abrir o arquivo." << endl;
        return;
    }

    float stackHeight = height / stacks;
    float deltaRadius = radius / stacks;

    // Gerar os pontos e triângulos da base
    for (int j = 0; j < slices; ++j) {
        float theta1 = 2 * M_PI * j / slices;
        float theta2 = 2 * M_PI * (j + 1) / slices;

        Point p1 = { radius * sin(theta1), 0, radius * cos(theta1)};
        Point p2 = {radius * sin(theta2), 0, radius * cos(theta2)};
        Point p3 = {0, 0, 0};

        // Escrever coordenadas da base no ficheiro
        outfile << p3.x << ";" << p3.y << ";" << p3.z << "\n"
        << p2.x << ";" << p2.y << ";" << p2.z << "\n"
        << p1.x << ";" << p1.y << ";" << p1.z << "\n";

    }

    for (int i = 0; i < stacks; ++i) {
        float currentRadius = radius - i * deltaRadius;
        float nextRadius = radius - (i + 1) * deltaRadius;
        float currentY = i * stackHeight;
        float nextY = (i + 1) * stackHeight;

        for (int j = 0; j < slices; ++j) {
            float theta1 = 2 * M_PI * j / slices;
            float theta2 = 2 * M_PI * (j + 1) / slices;

            Point p1 = {currentRadius * sin(theta1), currentY, currentRadius * cos(theta1)};
            Point p2 = {currentRadius * sin(theta2), currentY, currentRadius * cos(theta2)};
            Point p3 = {nextRadius * sin(theta1), nextY, nextRadius * cos(theta1)};
            Point p4 = {nextRadius * sin(theta2), nextY, nextRadius * cos(theta2)};

            // Escrever coordenadas no ficheiro
            outfile << p2.x << ";" << p2.y << ";" << p2.z << "/"
            << p3.x << ";" << p3.y << ";" << p3.z << "/"
            << p1.x << ";" << p1.y << ";" << p1.z << "\n";


            outfile << p2.x << ";" << p2.y << ";" << p2.z << "/"
            << p4.x << ";" << p4.y << ";" << p4.z << "/"
            << p3.x << ";" << p3.y << ";" << p3.z << "\n";
        }
    }
    outfile.close();
}

//Ring points
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

        pt1 = {x1, 0, z1};
        pt2 = {x2, 0, z2};
        pt3 = {x3, 0, z3};
        pt4 = {x4, 0, z4};

        // Escrever triângulos no arquivo
        outfile << pt1.x << ";" << pt1.y << ";" << pt1.z << "/"
                << pt2.x << ";" << pt2.y << ";" << pt2.z << "/"
                << pt4.x << ";" << pt4.y << ";" << pt4.z << "\n";

        outfile << pt2.x << ";" << pt2.y << ";" << pt2.z << "/"
                << pt3.x << ";" << pt3.y << ";" << pt3.z << "/"
                << pt4.x << ";" << pt4.y << ";" << pt4.z << "\n";

        // Gerar os pontos inferiores invertendo a ordem
        pt1.y = pt2.y = pt3.y = pt4.y = -pt1.y;

        // Escrever triângulos inferiores no arquivo
        outfile << pt2.x << ";" << pt2.y << ";" << pt2.z << "/"
                << pt1.x << ";" << pt1.y << ";" << pt1.z << "/"
                << pt4.x << ";" << pt4.y << ";" << pt4.z << "\n";

        outfile << pt4.x << ";" << pt4.y << ";" << pt4.z << "/"
                << pt3.x << ";" << pt3.y << ";" << pt3.z << "/"
                << pt2.x << ";" << pt2.y << ";" << pt2.z << "\n";
    }

    outfile.close();
}

// Bezier
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

    vector<vector<Point>> res(4, vector<Point>(4, {0, 0, 0}));

    for (int i = 0; i < 4; ++i) {
        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 4; ++k) {
                Point point = p[k][j];
                float x = point.x;
                float y = point.y;
                float z = point.z;
                res[i][j] = {res[i][j].x + x * m[i][k], res[i][j].y + y * m[i][k], res[i][j].z + z * m[i][k]};
            }
        }
    }

    vector<vector<Point>> result(4, vector<Point>(4, {0, 0, 0}));

    for (int i = 0; i < 4; ++i) {
        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 4; ++k) {
                Point point = res[i][k];
                float x = point.x;
                float y = point.y;
                float z = point.z;
                result[i][j] = {result[i][j].x + x * m[k][j], result[i][j].y + y * m[k][j], result[i][j].z + z * m[k][j]};
            }
        }
    }
    return result;
}

string calcBezier(Point p[4][4], int tessellation){
    float pu=0, pv=0;
    vector<vector<Point>> grid(tessellation+1, vector<Point>(tessellation+1));
    for (int j = 0; j <= tessellation; j++) {
        for (int k = 0; k <= tessellation; k++) {
            grid[j][k] = {0,0,0};
        }
    }
    float m[4][4] = {{-1, 3,  -3, 1},
                     {3, -6, 3, 0},
                     {-3, 3, 0, 0},
                     {1, 0, 0, 0}};
    string res;

    vector<vector<Point>> matrix = matrixMultiplication(p, m);

    float step = 1.f/(float)tessellation;
    for (int i = 0; i <= tessellation; i++) {
        pu = (float)i*step;
        float u_vector[4] = {(float)pow(pu,3), (float)pow(pu,2),pu,1};
        for (int j = 0; j <= tessellation; j++) {
            pv = (float)j*step;
            float v_vector[4] = {(float)pow(pv,3), (float)pow(pv,2),pv,1};
            grid[i][j] = multMatrixVector(u_vector, matrix, v_vector);
        }
    }

    for (int j = 0; j < tessellation; j++) {
        for (int k = 0; k < tessellation; k++) {

            res += to_string(grid[j][k].x) + ";" + to_string(grid[j][k].y) + ";" + to_string(grid[j][k].z) + "/" +
                   to_string(grid[j+1][k].x) + ";" + to_string(grid[j+1][k].y) + ";" + to_string(grid[j+1][k].z) + "/" +
                   to_string(grid[j][k+1].x) + ";" + to_string(grid[j][k+1].y) + ";" + to_string(grid[j][k+1].z) + "\n";

            res +=  to_string(grid[j+1][k].x) + ";" + to_string(grid[j+1][k].y) + ";" + to_string(grid[j+1][k].z) + "/" +
                    to_string(grid[j+1][k+1].x) + ";" + to_string(grid[j+1][k+1].y) + ";" + to_string(grid[j+1][k+1].z) + "/" +
                    to_string(grid[j][k+1].x) + ";" + to_string(grid[j][k+1].y) + ";" + to_string(grid[j][k+1].z) + "\n";
        }
    }
    return res;
}


bool bezier(string inFile, int tessellation, string outFile){
    if (tessellation < 0) return false;

    if (!readFile(inFile)) return false;

    string outputPath = "../Models/" + outFile;
    ofstream outputFile(outputPath);
    if (!outputFile.is_open()) {
        return false;
    }

    for (const auto& patch : patches) {
        Point m[4][4] = {0};
        auto it = patch.begin();
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                m[row][column] = points[*it];
                ++it;
            }
        }
        string bezierResult = calcBezier(m, tessellation);
        outputFile << bezierResult;
    }
    outputFile.close();
    return true;
}


int main(int argc, char* argv[]) {
    if (argc < 5 || argc > 7) {
        cout << "Usage: " << argv[0] << " <shape> <shape_parameters> <output_file> [additional_parameters]" << endl;
        return 1;
    }

    string shape = argv[1];
    string filename = argv[argc - 1]; // Output file name

    if (shape == "plane" || shape == "box" || shape == "sphere" || shape == "cone" || shape == "ring") {
        // Parse shape parameters based on shape type
        if (argc != 5) {
            cout << "Incorrect number of arguments for creating " << shape << "." << endl;
            return 1;
        }
        float length_or_radius = stof(argv[2]);
        int divisions_or_slices = stoi(argv[3]);

        // Call corresponding shape creation function
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
        string bezierFile = argv[2]; // Bezier control points file
        int tessellation = stoi(argv[3]);

        // Call bezier function
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

