<script setup>
const props = defineProps({
    project: {
        type: Object,
        required: false,
    },
});

// delete handled here?

const emit = defineEmits(["select", "delete"]);

const selectProject = () => {
    emit("select", props.project);
};

const deleteProject = (event) => {
    event.stopPropagation();  // so it doesnt trigger select
    emit("delete", props.project);
};
</script>

<template>
    <div class="project-card" @click="selectProject">
        <div class="project-header">
            <h3 class="project-name">{{ project.name }}</h3>
            <!--<span class="project-date">{{ project.createdAt }}</span>-->
        </div>
        <div class="project-actions">
            <button class="access-button" @click.stop="selectProject">Abrir</button>
            <button class="delete-button" @click="deleteProject">Remover</button>
        </div>
    </div>
</template>

<style scoped>
.project-card {
    border: 1px solid #e5e7eb;
    padding: 1.5rem;
    border-radius: 0.75rem;
    background-color: #fff;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    margin-bottom: 1rem;
}

.project-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    border-color: #1e40af20;
}

.project-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.project-name {
    font-size: 1.25rem;
    font-weight: 600;
    color: #1f2937;
}

.project-date {
    font-size: 0.875rem;
    color: #6b7280;
}

.project-actions {
    display: flex;
    gap: 0.75rem;
    margin-top: 0.5rem;
}

.access-button,
.delete-button {
    padding: 0.5rem 1rem;
    border-radius: 0.375rem;
    cursor: pointer;
    font-size: 0.875rem;
    font-weight: 500;
    transition: all 0.2s ease;
}

.access-button {
    background-color: #1e40af;
    color: white;
    box-shadow: 0 1px 2px rgba(30, 64, 175, 0.2);
}

.access-button:hover {
    background-color: #1e3a8a;
    transform: translateY(-1px);
}

.delete-button {
    background-color: #fee2e2;
    color: #dc2626;
}

.delete-button:hover {
    background-color: #fecaca;
    transform: translateY(-1px);
}

</style>
