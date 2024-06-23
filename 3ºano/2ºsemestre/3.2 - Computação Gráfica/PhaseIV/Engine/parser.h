#include "tinyxml2.h"
#include "structs.h"
#include <GL/glew.h>
#include <GL/freeglut.h>
#include <GL/glut.h>
#include <IL/il.h>
#include <IL/ilu.h>
#include <IL/ilut.h>

#include <iostream>
#include <fstream>
using namespace tinyxml2;

#ifndef ENGINE_PARSER_H
#define ENGINE_PARSER_H

map<string, int> textureIDs;


int loadTexture(std::string s) {
    unsigned int t,tw,th;
    unsigned char *texData;
    unsigned int texID;

    // Iniciar o DevIL
    ilInit();

    // Colocar a origem da textura no canto inferior esquerdo
    ilEnable(IL_ORIGIN_SET);
    ilOriginFunc(IL_ORIGIN_LOWER_LEFT);

    // Carregar a textura para memória
    ilGenImages(1,&t);
    ilBindImage(t);
    ilLoadImage((ILstring)s.c_str());
    tw = ilGetInteger(IL_IMAGE_WIDTH);
    th = ilGetInteger(IL_IMAGE_HEIGHT);

    // Assegurar que a textura se encontra em RGBA (Red, Green, Blue, Alpha) com um byte (0 -255) por componente
    ilConvertImage(IL_RGBA, IL_UNSIGNED_BYTE);
    texData = ilGetData();

    // Gerar a textura para a placa gráfica
    glGenTextures(1,&texID);

    glBindTexture(GL_TEXTURE_2D,texID);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

    // Upload dos dados de imagem
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, tw, th, 0, GL_RGBA, GL_UNSIGNED_BYTE, texData);
    glGenerateMipmap(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, 0);
    return texID;
}

bool readFile(World* world, const string& filename) {
    string fullFilePath =  "../Models/" + filename;
    ifstream infile(fullFilePath);
    if (!infile) return false;

    vector<Point> pontos;
    ModelData modelData;

    string line;
    while (getline(infile, line)) {
        istringstream iss(line);
        string triangleStr;
        while (getline(iss, triangleStr, '/')) {
            istringstream triangleIss(triangleStr);
            Point pt;
            char delimiter;
            if (!(triangleIss >> pt.x >> delimiter >> pt.y >> delimiter >> pt.z >> delimiter >> pt.nx >> delimiter >> pt.ny >> delimiter >> pt.nz >> delimiter >> pt.tx >> delimiter >> pt.ty)) return false;
            pontos.push_back(pt);
            Point2 pt2;
            pt2.x = pt.x;
            pt2.y = pt.y;
            pt2.z = pt.z;

            modelData.vertexPoints.push_back(pt2);

            Point2 pt3;
            pt3.x = pt.nx;
            pt3.y = pt.ny;
            pt3.z = pt.nz;

            modelData.normalPoints.push_back(pt3);

            Point3 pt4;
            pt4.x = pt.tx;
            pt4.y = pt.ty;

            modelData.texturePoints.push_back(pt4);







        }
    }
    world->modelData.insert(make_pair(filename, modelData));
    world->points.insert(make_pair(filename, pontos));

    modelData.vertexCount=  modelData.vertexPoints.size();
    return true;
}

bool parseLights(XMLElement* pLights, World* world) {
    if (!pLights) return false;

    XMLElement* pLight = pLights->FirstChildElement("light");
    while (pLight) {
        Light light;
        const char* type = pLight->Attribute("type");
        if (type) light.type = type;

        if (light.type == "point") {
            pLight->QueryFloatAttribute("posx", &light.position.x);
            pLight->QueryFloatAttribute("posy", &light.position.y);
            pLight->QueryFloatAttribute("posz", &light.position.z);
        } else if (light.type == "directional") {
            pLight->QueryFloatAttribute("dirx", &light.direction.x);
            pLight->QueryFloatAttribute("diry", &light.direction.y);
            pLight->QueryFloatAttribute("dirz", &light.direction.z);
        } else if (light.type == "spot") {
            pLight->QueryFloatAttribute("posx", &light.position.x);
            pLight->QueryFloatAttribute("posy", &light.position.y);
            pLight->QueryFloatAttribute("posz", &light.position.z);
            pLight->QueryFloatAttribute("dirx", &light.direction.x);
            pLight->QueryFloatAttribute("diry", &light.direction.y);
            pLight->QueryFloatAttribute("dirz", &light.direction.z);
            pLight->QueryFloatAttribute("cutoff", &light.cutoff);
        }
        world->lights.push_back(light);
        pLight = pLight->NextSiblingElement("light");
    }
    return true;
}

/*  cores
void parseColor(XMLElement* colorTag, Group* group){
    const char * attribute = nullptr;
    float x = 1, y = 1, z = 1;
    if (colorTag != nullptr) {
        attribute = colorTag->Attribute("X");
        if (attribute != nullptr)
            x = stof(attribute);
        attribute = colorTag->Attribute("Y");
        if (attribute != nullptr)
            y = stof(attribute);
        attribute = colorTag->Attribute("Z");
        if (attribute != nullptr)
            z = stof(attribute);
    }
    Color *color = new Color(x,y,z);
    group->transform.color = color;
}
*/

bool parseGroup(XMLElement *pGroup, Group &parentGroup, bool isFirstGroup, World* world) {
    Group group;

    //parseColor(pGroup->FirstChildElement("color"), &group);

    // Parsing das transformações do grupo
    XMLElement *pTransform = pGroup->FirstChildElement("transform");
    if (pTransform) {
        // Verifica se há elementos de transformação dentro do transform
        for (XMLElement *pTransformElement = pTransform->FirstChildElement(); pTransformElement; pTransformElement = pTransformElement->NextSiblingElement()) {
            const char *transformType = pTransformElement->Name();
            if (strcmp(transformType, "translate") == 0) {
                group.transform.order.push_back("translate");
                pTransformElement->QueryFloatAttribute("x", &group.transform.translate.x);
                pTransformElement->QueryFloatAttribute("y", &group.transform.translate.y);
                pTransformElement->QueryFloatAttribute("z", &group.transform.translate.z);


                float time=-1;
                if (pTransformElement->QueryFloatAttribute("time", &time) == XML_SUCCESS) {
                    group.transform.translate.time = time;
                }

                bool align = false;
                if (pTransformElement->QueryBoolAttribute("align", &align) == XML_SUCCESS) {
                    group.transform.translate.align = align;
                }

                vector<Point> controlPoints;
                XMLElement *pPoint = pTransformElement->FirstChildElement("point");
                while (pPoint != nullptr) {
                    float x, y, z;
                    pPoint->QueryFloatAttribute("x", &x);
                    pPoint->QueryFloatAttribute("y", &y);
                    pPoint->QueryFloatAttribute("z", &z);

                    controlPoints.push_back(Point{x, y, z});
                    pPoint = pPoint->NextSiblingElement("point");

                }

                group.catmullCurves.push_back(CatmullCurve{controlPoints});


            } else if (strcmp(transformType, "scale") == 0) {
                group.transform.order.push_back("scale");
                pTransformElement->QueryFloatAttribute("x", &group.transform.scale.x);
                pTransformElement->QueryFloatAttribute("y", &group.transform.scale.y);
                pTransformElement->QueryFloatAttribute("z", &group.transform.scale.z);
            } else if (strcmp(transformType, "rotate") == 0) {
                group.transform.order.push_back("rotate");
                float angle;
                float time = -1;
                if (pTransformElement->QueryFloatAttribute("time", &time) == XML_SUCCESS) {
                    angle = fmod(angle, 360.0f);
                    if (angle < 0) angle += 360.0f;
                }
                else {
                    pTransformElement->QueryFloatAttribute("angle", &angle);
                }

                float x, y, z;
                pTransformElement->QueryFloatAttribute("x", &x);
                pTransformElement->QueryFloatAttribute("y", &y);
                pTransformElement->QueryFloatAttribute("z", &z);
                group.transform.rotate.angle = angle;
                group.transform.rotate.time = time;
                group.transform.rotate.x = x;
                group.transform.rotate.y = y;
                group.transform.rotate.z = z;
            }
        }
    }

    // Verifica se há modelos no grupo
    XMLElement *pModels = pGroup->FirstChildElement("models");
    if (pModels) {
        XMLElement *pModel = pModels->FirstChildElement("model");
        while (pModel != nullptr) {
            const char *strfile = pModel->Attribute("file");
            string namefile = strfile;
            world->files.push_back(namefile);

            if (!readFile(world, namefile)) {
                return false;
            }


            Model model;
            model.file = strfile;
            // Parsing das transformações do modelo nao testado

            XMLElement *pTexture = pModel->FirstChildElement("texture");
            if (pTexture) {
                const char * textureName = pTexture -> Attribute("file");
                string texturePath = "../Models/";
                texturePath += textureName;

                if (textureIDs.find(textureName) != textureIDs.end()){
                    auto it = textureIDs.find(textureName);
                    model.textureId = it->second;
                } else {
                    model.textureId = loadTexture(texturePath);
                    textureIDs.insert({textureName, model.textureId});
                }
            }



            // Parsing das propriedades de cor
            XMLElement *pColor = pModel->FirstChildElement("color");
            if (pColor != nullptr)
            {
                XMLElement *pDiffuse = pColor->FirstChildElement("diffuse");
                if (pDiffuse != nullptr)
                {
                    pDiffuse->QueryFloatAttribute("R", &model.color.diffuse.R);
                    pDiffuse->QueryFloatAttribute("G", &model.color.diffuse.G);
                    pDiffuse->QueryFloatAttribute("B", &model.color.diffuse.B);
                }
                XMLElement *pAmbient = pColor->FirstChildElement("ambient");
                if (pAmbient != nullptr)
                {
                    pAmbient->QueryFloatAttribute("R", &model.color.ambient.R);
                    pAmbient->QueryFloatAttribute("G", &model.color.ambient.G);
                    pAmbient->QueryFloatAttribute("B", &model.color.ambient.B);
                }
                XMLElement *pSpecular = pColor->FirstChildElement("specular");
                if (pSpecular != nullptr)
                {
                    pSpecular->QueryFloatAttribute("R", &model.color.specular.R);
                    pSpecular->QueryFloatAttribute("G", &model.color.specular.G);
                    pSpecular->QueryFloatAttribute("B", &model.color.specular.B);
                }
                XMLElement *pEmissive = pColor->FirstChildElement("emissive");
                if (pEmissive != nullptr)
                {
                    pEmissive->QueryFloatAttribute("R", &model.color.emissive.R);
                    pEmissive->QueryFloatAttribute("G", &model.color.emissive.G);
                    pEmissive->QueryFloatAttribute("B", &model.color.emissive.B);
                }
                XMLElement *pShininess = pColor->FirstChildElement("shininess");
                if (pShininess != nullptr)
                {
                    pShininess->QueryFloatAttribute("value", &model.color.shininess);
                }


            }

            group.models.push_back(model);
            pModel = pModel->NextSiblingElement("model");
        }
    }

    // Parsing dos grupos aninhados
    XMLElement *pNestedGroup = pGroup->FirstChildElement("group");
    if (pNestedGroup) {
        while (pNestedGroup != nullptr) {
            parseGroup(pNestedGroup, group, false, world);
            pNestedGroup = pNestedGroup->NextSiblingElement("group");
        }
    }

    if (isFirstGroup) {
        // Primeiro grupo é o grupo pai
        parentGroup = group;
    } else {
        // Adiciona o grupo atual ao grupo pai
        parentGroup.groups.push_back(group);
    }
    return true;
}


bool parseGroups(XMLElement *pRoot, World *world) {
    XMLElement *pGroup = pRoot->FirstChildElement("group");
    if (!pGroup) {
        cerr << "No groups found." << endl;
        return false;
    }

    while (pGroup) {
        Group group;

        parseGroup(pGroup, group, true, world);

        // Adicione o grupo principal ao mundo
        world->groups.push_back(group);

        // Avance para o próximo grupo
        pGroup = pGroup->NextSiblingElement("group");
    }

    return true;
}

bool parseXML(const char *xmlFile, World *world, float &radius, float &betA, float &alpha) {
    XMLDocument xmlDoc;
    if (xmlDoc.LoadFile(xmlFile) != XML_SUCCESS) {
        cerr << "Error loading XML file: " << xmlFile << endl;
        return false;
    }

    XMLElement *pRoot = xmlDoc.FirstChildElement("world");
    if (pRoot == nullptr) {
        std::cerr << "Invalid XML format: No 'world' element found." << std::endl;
        return false;
    }

    // Parsing dos elementos da window
    XMLElement *pWindow = pRoot->FirstChildElement("window");
    if (!pWindow) {
        cerr << "No window element found." << endl;
        return false;
    }

    int width, height;
    pWindow->QueryIntAttribute("width", &width);
    pWindow->QueryIntAttribute("height", &height);

    // Parsing dos elementos da câmara
    XMLElement *pCamera = pRoot->FirstChildElement("camera");
    if (pCamera) {
        XMLElement *pPosition = pCamera->FirstChildElement("position");
        if (pPosition) {
            float x, y, z;
            pPosition->QueryFloatAttribute("x", &x);
            pPosition->QueryFloatAttribute("y", &y);
            pPosition->QueryFloatAttribute("z", &z);

            world->camera.position[0] = x;
            world->camera.position[1] = y;
            world->camera.position[2] = z;
        }

        XMLElement *pLookat = pCamera->FirstChildElement("lookAt");
        if (pLookat) {
            float x, y, z;
            pLookat->QueryFloatAttribute("x", &x);
            pLookat->QueryFloatAttribute("y", &y);
            pLookat->QueryFloatAttribute("z", &z);

            world->camera.lookAt[0] = x;
            world->camera.lookAt[1] = y;
            world->camera.lookAt[2] = z;
        }

        XMLElement *pUp = pCamera->FirstChildElement("up");
        if (pUp) {
            float x, y, z;
            pUp->QueryFloatAttribute("x", &x);
            pUp->QueryFloatAttribute("y", &y);
            pUp->QueryFloatAttribute("z", &z);

            world->camera.up[0] = x;
            world->camera.up[1] = y;
            world->camera.up[2] = z;
        }

        XMLElement *pProjection = pCamera->FirstChildElement("projection");
        if (pProjection) {
            float x, y, z;
            pProjection->QueryFloatAttribute("fov", &x);
            pProjection->QueryFloatAttribute("near", &y);
            pProjection->QueryFloatAttribute("far", &z);

            world->camera.projection[0] = x;
            world->camera.projection[1] = y;
            world->camera.projection[2] = z;
        }
    } else {
        cerr << "No camera element found." << endl;
        return false;
    }
        XMLElement* pLights = pRoot->FirstChildElement("lights");
    parseLights(pLights, world);

    if (!parseGroups(pRoot, world)) {
        cerr << "Error parsing groups." << endl;
        return false;
    }

    radius = sqrt(pow(world->camera.position[0], 2) + pow(world->camera.position[1], 2) + pow(world->camera.position[2], 2));
    betA = asin(world->camera.position[1] / radius);
    alpha = asin(world->camera.position[0] / sqrt(pow(world->camera.position[2], 2) + pow(world->camera.position[0], 2)));

    return true;
}

#endif //ENGINE_PARSER_H