// ficheiro LPSOLVE
// Função objetivo
min: 11 z1 + 10 z2 + 7 z3;

//restrições
//grupo I : conservação de fluxo
NODE0: z1 + z2 + z3 - x0_5 - x0_4 - x0_3 - x0_2 = 0;
//NODE1: não existe
NODE2: x0_2 - x2_4 - x2_3 = 0;
NODE3: x0_3 - x3_6 - x3_5 + x2_3 - x3_4 = 0;
NODE4: x0_4 - x4_8 - x4_7 + x2_4 - x4_6 + x3_4 - x4_5 = 0;
NODE5: x0_5 - x5_10 - x5_9 + x3_5 - x5_7 + x4_5 - x5_6 = 0;
NODE6: x3_6 - x6_9 +x4_6 - x6_8 + x5_6 - x6_7 = 0;
NODE7: - z3 + x4_7 - x7_10 + x5_7 -x7_9 + x6_7 - x7_8 = 0;
NODE8: x4_8 - x8_11 + x6_8 - x8_10 + x7_8 - x8_9 = 0;
NODE9: x5_9 + x6_9 + x7_9 - x9_11 + x8_9 -x9_10 = 0;
NODE10: -z2 + x5_10 + x7_10 + x8_10 + x9_10 - x10_11 = 0;
NODE11: -z1 + x8_11 + x9_11 + x10_11 = 0;


// grupo II : Itens
//Cada item é alocado em apenas um bin 


//ITEM1: x2_3 + x3_4 + x4_5 + x5_6 + x6_7 +x7_8 + x8_9 + x9_10 z10_11 >= 0; (não existe)
ITEM2: x0_2 + x2_4 + x3_5 + x4_6 + x5_7 + x6_8 + x7_9 + x8_10 + x9_11 >=10;
ITEM3: x0_3 + x3_6 + x4_7 + x6_9 + x7_10 + x8_11 >=10;
ITEM4: x0_4 + x4_8 + x5_9 >=11;
ITEM5: x0_5 + x5_10 >=5;

// grupo III : Contentores
// Para cada bin, a soma dos comprimentos dos seus itens não excede a sua capacidade máxima

BIN1: z1 <= 30000;
BIN2: z2 <=1;
BIN3: z3 <=10;


// variáveis inteiras
int 
z1, z2, z3
x0_5, x5_10, 
x0_4, x4_8, x5_9, 
x0_3, x3_6, x4_7, x6_9, x7_10, x8_11, 
x0_2, x2_4, x3_5, x4_6, x5_7, x6_8, x7_9, x8_10, x9_11, 
x2_3, x3_4, x4_5, x5_6, x6_7, x7_8, x8_9, x9_10, x10_11;

