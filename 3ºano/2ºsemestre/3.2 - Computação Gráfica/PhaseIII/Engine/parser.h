#include "tinyxml2.h"
#include "structs.h"

using namespace tinyxml2;

#ifndef ENGINE_PARSER_H
#define ENGINE_PARSER_H

bool readFile(World* world, const string& filename) {
    string fullFilePath =  "../Models/" + filename;
    ifstream infile(fullFilePath);
    if (!infile) return false;

    vector<Point> pontos;

    string line;
    while (getline(infile, line)) {
        istringstream iss(line);
        string triangleStr;
        while (getline(iss, triangleStr, '/')) {
            istringstream triangleIss(triangleStr);
            Point pt;
            char delimiter;
            if (!(triangleIss >> pt.x >> delimiter >> pt.y >> delimiter >> pt.z)) return false;
            pontos.push_back(pt);
        }
    }
    world->points.insert(make_pair(filename, pontos));
    return true;
}

// cores
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


bool parseGroup(XMLElement *pGroup, Group &parentGroup, bool isFirstGroup, World* world) {
    Group group;

    parseColor(pGroup->FirstChildElement("color"), &group);

    // Parsing das transformações do grupo
    XMLElement *pTransform = pGroup->FirstChildElement("transform");
    if (pTransform) {
        // Verifica se há elementos de transformação dentro do transform
        for (XMLElement *pTransformElement = pTransform->FirstChildElement(); pTransformElement; pTransformElement = pTransformElement->NextSiblingElement()) {
            const char *transformType = pTransformElement->Name();
            if (strcmp(transformType, "translate") == 0) {
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
                pTransformElement->QueryFloatAttribute("x", &group.transform.scale.x);
                pTransformElement->QueryFloatAttribute("y", &group.transform.scale.y);
                pTransformElement->QueryFloatAttribute("z", &group.transform.scale.z);
            } else if (strcmp(transformType, "rotate") == 0) {
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
