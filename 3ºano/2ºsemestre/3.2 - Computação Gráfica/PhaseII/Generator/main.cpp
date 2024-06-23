#include <iostream>
#include <vector>
#include <string>
#include <fstream>
#include <cmath> // Para funções matemáticas como sin, cos, etc.

using namespace std;

// Definição da estrutura Point
struct Point {
    float x;
    float y;
    float z;
};

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



int main(int argc, char* argv[]) {
    if (argc < 5 || argc > 7) {
        cout << "Uso: " << argv[0] << " <forma> <comprimento/raio> <divisões/fatias> <arquivo_saida> [pilhas]" << endl;
        return 1;
    }

    string shape = argv[1];
    float length_or_radius = stof(argv[2]);
    int divisions_or_slices = stoi(argv[3]);
    string filename = argv[argc - 1]; // Nome do arquivo de saída

    if (shape == "plane") {
        if (argc != 5) {
            cout << "Número incorreto de argumentos para criar um plano." << endl;
            return 1;
        }
        createPlane(length_or_radius, divisions_or_slices, filename);
    } else if (shape == "box") {
        if (argc != 5) {
            cout << "Número incorreto de argumentos para criar uma caixa." << endl;
            return 1;
        }
        createBox(length_or_radius, divisions_or_slices, filename);
    } else if (shape == "sphere") {
        if (argc != 6) {
            cout << "Número incorreto de argumentos para criar uma esfera." << endl;
            return 1;
        }
        float radius = length_or_radius;
        int slices = divisions_or_slices;
        int stacks = stoi(argv[4]);
        createSphere(radius, slices, stacks, filename);
    } else if (shape == "cone") {
        if (argc != 7) {
            cout << "Número incorreto de argumentos para criar um cone." << endl;
            return 1;
        }
        float radius = stof(argv[2]);
        float height = stof(argv[3]);
        int slices = stoi(argv[4]);
        int stacks = stoi(argv[5]);
        createCone(radius, height, slices, stacks, filename);

    }else if (shape == "ring") {
        if (argc != 6) {
            cout << "Número incorreto de argumentos para criar um anel." << endl;
            return 1;
        }
        float inner_radius = length_or_radius;
        float outer_radius = divisions_or_slices;
        float ring_slices = stof(argv[4]);
        createRing(inner_radius, outer_radius, ring_slices, filename);
     } else {
        cout << "Formato de forma inválido. Use 'plane', 'box', 'sphere' ou 'cone'." << endl;
        return 1;
    }

    return 0;
}
