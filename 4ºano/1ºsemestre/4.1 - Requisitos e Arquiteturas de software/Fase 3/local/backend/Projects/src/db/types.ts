export enum ToolProcedure {
    CROP = 'crop', // recortar
    SCALE = 'scale', // alterar escala
    BORDER = 'border', // adicionar borda
    BRIGHTNESS = 'brightness', // alterar brilho
    CONTRAST = 'contrast', // alterar contraste
    ROTATE = 'rotate', // rodar
    AUTO_CROP = 'autocrop', // recortar automaticamente
    EXTRACT_TEXT = 'extracttext', // extrair texto
    OBJECT_RECOGNITION = 'objectrecognition', // reconhecimento de objetos
    PEOPLE_COUNT = 'peoplecount', // contagem de pessoas
    WATERMARK = 'watermark',
    REMOVE_BG = 'removebg'
}


export const AvailableToolsPerUser = {
    free: [
        ToolProcedure.CROP,
        ToolProcedure.BORDER,
        ToolProcedure.CONTRAST,
        ToolProcedure.ROTATE,
        ToolProcedure.SCALE,
        ToolProcedure.BRIGHTNESS,
    ] as const,
    anonymous: [ToolProcedure.SCALE, ToolProcedure.BRIGHTNESS] as const,
    premium: [
        ToolProcedure.CROP,
        ToolProcedure.BORDER,
        ToolProcedure.CONTRAST,
        ToolProcedure.ROTATE,
        ToolProcedure.SCALE,
        ToolProcedure.BRIGHTNESS,
        ToolProcedure.AUTO_CROP,
        ToolProcedure.EXTRACT_TEXT,
        ToolProcedure.OBJECT_RECOGNITION,
        ToolProcedure.PEOPLE_COUNT,
        ToolProcedure.WATERMARK,
        ToolProcedure.REMOVE_BG,
    ] as const,
} as const;

export type UserType = keyof typeof AvailableToolsPerUser;



export const ParamsPerTool = {
    [ToolProcedure.CROP]: { left: 'number', upper: 'number', right: 'number', lower: 'number' },
    [ToolProcedure.SCALE]: { new_width: 'number', new_height: 'number' },
    [ToolProcedure.BORDER]: { border_width: 'number', r: 'number', g: 'number', b: 'number' },
    [ToolProcedure.BRIGHTNESS]: { brightness_factor: 'number' },
    [ToolProcedure.CONTRAST]: { contrast_factor: 'number' },
    [ToolProcedure.ROTATE]: { angle: 'number' },
    [ToolProcedure.AUTO_CROP]: null,
    [ToolProcedure.EXTRACT_TEXT]: null,
    [ToolProcedure.OBJECT_RECOGNITION]: { model: 'string' },
    [ToolProcedure.PEOPLE_COUNT]: null,
    [ToolProcedure.REMOVE_BG]: null,
    [ToolProcedure.WATERMARK]: { scale_factor: 'number', opacity: 'number', positionX: 'number', positionY: 'number' },
} as const;

export type ToolParams = typeof ParamsPerTool;
