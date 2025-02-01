// Utilities
import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useProjectStore = defineStore('project', () => {

    const projects = ref([]); 
    const currentProject = ref(null) // not sure if needed
    const images = ref([]) // will store images for the current project

    const fetchProjects = async (userId) => {
        try {
            // not sure if its setup already
            const response = await axios.get(`/api/projects/project/user/${userId}`, {withCredentials: true});
            projects.value = response.data
            //console.log(projects.value)
        }
        catch (error) {
            console.error(error)
        }
    }

    const createProject = async (userId, name) => {
        try {
            console.log('user making a project')
            console.log(userId)
            console.log(name)
            const response = await axios.post(`/api/projects/project/`, {    
                userId,
                name
            }, {
                withCredentials: true, // Required to send cookies across domains
              });
            console.log(response.data)
            const newProject = response.data;
            projects.value.push(response.data)

            return newProject;
        }
        catch (error) {
            console.error(error);
        }
    }

    const setCurrentProject = (project_id) => {
        currentProject.value = projects.value.find((project) => project.id === project_id)
        images.value = [];
    }

    const uploadImages = async (files, projectId) => {
        if (!Array.isArray(files)) {
            files = [files];
        }

        // https://dev.to/shieldstring/file-uploads-with-axios-a-beginners-guide-1lmd
        try {
            console.log(projectId)
            const formData = new FormData();
            files.forEach(file => {
                formData.append('file', file);
            });
    
            const response = await axios.post(
                `/api/projects/image/${projectId}`,
                formData,
                {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                    withCredentials: true // Include cookies for session-based auth
                }
            );
    
            if (response.status === 200) {
                console.log('Image uploaded');
                // response has the url 
                //images.value.push(response.data.uri);  // uri??
                console.log(response.data)
                return response.data;
            } else {
                console.error('err uploading image:', response.data.message);
                return null;
            }
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    const uploadZip = async (zipFile, projectId) => {
        try {

    
            if (zipFile.type !== 'application/zip' && !zipFile.name.endsWith('.zip')) {
                console.log("invalid type")
            }
    
            const formData = new FormData();
            formData.append('file', zipFile); 
    
            console.log('Uploading zip:', zipFile.name);
            console.log('Project ID:', projectId);
            
            const response = await axios.post(
                `/api/projects/image/zip/${projectId}`,
                formData,
                {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                    withCredentials: true
                }
            );
    
            if (response.status === 400) {
                const errorMessage = response.data.message;
                console.error('Validation error:', errorMessage);
            }
    
            if (response.status === 200) {
                console.log('ZIP uploaded successfully');
                return response.data;
            }
        
        } catch (error) {
            console.error(error)
        }
    };

    const deleteImage = async (projectId, imageId) => {
        try {
            const response = await axios.delete(`/api/projects/image/${projectId}/${imageId}`, { withCredentials: true })

            if (response.status === 200){
                console.log(response.data)
            }
        }
        catch (error) {
            console.log(error)
        }
    }

    const downloadImage = async (project_name, projectId) => {
        try {
            console.log(project_name)
            console.log(projectId)
    
            const response = await axios.post(`/api/projects/image/download/${projectId}`, {}, {
                responseType: 'blob',
            });
        
            const blob = response.data;
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = `${project_name}-images.zip`;
        
            link.click();
        } catch (error) {
            console.error("Error downloading image:", error);
        }
    };
    

    const deleteProject = async (project_id) => {
        try {
            await axios.delete(`/api/projects/project/${project_id}`, {withCredentials: true});
            projects.value = projects.value.filter(project => project.id !== project_id);
        } catch (error) {
            console.error(error);
        }
    };

    const addTool = async (projectID, tool) => {
        console.log(projectID, tool)
        try {
            const response = await axios.post(`/api/projects/tool/${projectID}`, tool, {withCredentials: true}, {  
                headers: { 'Content-Type': 'multipart/form-data' },
            });
            return response.data;
        }
        catch (error) {
            console.log(error);
        }
    }

    const removeTool = async (projectId, toolId) => {
        console.log("here");
        console.log(projectId);
        console.log(toolId);
        try {
            const response = await axios.delete(`/api/projects/tool/${projectId}/${toolId}`, { withCredentials: true })
            return response.data;
        } catch (error) {
            console.log("Error during delete:", error);
        }
    };
    

    const updateTool = async (projectId, toolId, parameters) => {
        try {
            const response = await axios.post(`/api/projects/tool/${projectId}`, {
                toolId,
                parameters,
              },
            {
                withCredentials: true
            });
        }
        catch (error) {
            console.log(error)
        }
    }

    const applyTools = async (projectId, toolIds) => {
        try {
            const response = await axios.post(`/api/projects/project/applyTools/${projectId}`,
                { toolIds }, 
                { withCredentials: true }
            );
            console.log(response);
            return response.data;
        } catch (error) {
            console.log(error);
        }
    }
    

    return {
        projects,
        currentProject,
        images,
        deleteImage,
        uploadImages,
        uploadZip,
        downloadImage,
        fetchProjects,
        createProject,
        setCurrentProject,
        deleteProject,
        addTool,
        removeTool,
        updateTool,
        applyTools
    }
})
