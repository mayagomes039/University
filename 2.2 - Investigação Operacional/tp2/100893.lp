

//função objetivo
min: 1 x12 + 0 x142 + 5 x152 + 7 x162 + 10 x172 + 13 x182 + 7 x192 + 5 x202 + 5 x212 + 
4 x13 + 8 x173 + 8 x193 + 8 x203 + 4 x213 + 
9 x14 + 5 x154 + 10 x174 + 13 x184 + 7 x194 + 5 x204 + 5 x214 + 
2 x15 + 6 x195 + 0 x205 + 6 x215 + 
10 x16 + 5 x196 + 6 x206 + 7 x216 + 
11 x17 + 5 x197 + 6 x217 + 
15 x18 + 
9 x19 + 
9 x110 + 
6 x111 + 
1 x1222 + 4 x1322 + 9 x1422+ 2 x1522+ 10 x1622 + 11 x1722 +
15 x1822 + 9  x1922 + 9 x2022 + 6 x2122 + i;

i = x12 + x13 + x14 + x15 + x16 + x17 + x18 + x19 + x110 + x111;
f = x1222 + x1322 + x1422 + x1522 + x1622 + x1722 + x1822 + x1922 + x2022 + x2122;
i = f;

/* Entry  Edge */
vertice2: x12 + x142 + x152 + x162 +x172 + x182 + x192 + x202 + x212 = 1;
vertice3: x13 + x173 +x193 + x203 + x213 = 1;
vertice4: x14 + x154 + x174 + x184 + x194 + x204 + x214 = 1;
vertice5: x15 + x195 + x205 + x215 = 1;
vertice6: x16 + x196 + x206 + x216 = 1;
vertice7: x17 + x197 + x217 = 1;
vertice8: x18 = 1;
vertice9: x19 = 1;
vertice10: x110 = 1; 
vertice11: x111 = 1;

/* Exit Edge*/
vertice12: x1222 = 1;
vertice13: x1322 = 1;
vertice14: x142 + x1422 = 1;
vertice15: x152 + x154 + x1522 = 1;
vertice16: x162 + x1622 = 1;
vertice17: x172 + x173 + x174 + x1722 = 1;
vertice18: x182 + x184 + x1822 = 1;
vertice19: x192 + x193 + x194 + x195 + x196 + x197 + x1922 = 1;
vertice20: x202 + x203 + x204 + x205 + x206 + x2022 = 1;
vertice21: x212 + x213 + x214 + x215 + x216 + x217 + x2122 = 1;
