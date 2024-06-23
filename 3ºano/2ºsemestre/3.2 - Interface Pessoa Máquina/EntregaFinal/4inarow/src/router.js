import { createRouter, createWebHistory } from 'vue-router';
import Home from './pages/LogIn.vue';
import ServicosGerais from './pages/ServicosGerais.vue';
import ServicosCombustao from './pages/ServicosCombustao.vue';
import ServicosEletricos from './pages/ServicosEletricos.vue';
import ServicosHibridos from './pages/ServicosHibridos.vue';
import DetalhesServico from './pages/DetalhesServico.vue';
import DetalhesServicoConcluido from './pages/DetalhesServicoConcluido.vue';
import EletricosAtribuidos from './pages/ServicosAtribuidosEletrico.vue';
import CombustaoAtribuidos from './pages/ServicosAtribuidosCombustao.vue';
import GeraisAtribuidos from './pages/ServicosAtribuidos.vue';
import HibridoAtribuidos from './pages/ServicosAtribuidosHibridos.vue';
import GeraisConcluidos from './pages/ServicosConcluidos.vue';
import EletricosConcluidos from './pages/ServicosConcluidosEletricos.vue';
import CombustaoConcluidos from './pages/ServicosConcluidosCombustao.vue';
import HibridoConcluidos from './pages/ServicosConcluidosHibridos.vue';
import ServicoEmAndamento from './pages/ServicoEmAndamento.vue';
import NotFound from './pages/NotFound.vue';
import LogOut from './pages/LogOut.vue';

const routes = [
  {
    path: '/',
    component: Home,
  },
  {
    path: '/servicos-gerais',
    component: ServicosGerais
  },
  {
    path: '/servicos-combustao',
    component: ServicosCombustao
  },
  {
    path: '/servicos-eletricos',
    component: ServicosEletricos
  },
  {
    path: '/servicos-hibridos',
    component: ServicosHibridos
  },
  {
    path: '/detalhes-servico/:id',
    component: DetalhesServico
  },
  {
    path: '/detalhes-servicoConcluido/:id',
    component: DetalhesServicoConcluido
  },
  {
    path: '/servicos-atribuidos-eletricos',
    component: EletricosAtribuidos
  },
  {
    path: '/servicos-atribuidos-combustao',
    component: CombustaoAtribuidos
  },
  {
    path: '/servicos-atribuidos',
    component: GeraisAtribuidos
  },
  {
    path: '/servicos-atribuidos-hibridos',
    component: HibridoAtribuidos
  },
  {
    path: '/servicos-concluidos',
    component: GeraisConcluidos
  },
  {
    path: '/servicos-concluidos-eletricos',
    component: EletricosConcluidos
  },
  {
    path: '/servicos-concluidos-combustao',
    component: CombustaoConcluidos
  },
  {
    path: '/servicos-concluidos-hibridos',
    component: HibridoConcluidos
  },
  {
    path: '/:notFound(.*)',
    component: NotFound
  },
  {
    path: '/servico-em-andamento/:id',
    component: ServicoEmAndamento
  },
  {
    path: '/logOut',
    component: LogOut
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

function verificaPrefixo(lista, str) {
  for (let i = 0; i < lista.length; i++) {
      if (str.startsWith(lista[i])) {
          return true; 
      }
  }
  return false; 
}

// Verifique se o usuário está logado antes de cada navegação
router.beforeEach((to, from, next) => {
  const allowedRoutes1 = [ '/','/logOut', '/detalhes-servico/','/detalhes-servicoConcluido/', '/servicos-combustao', '/servicos-atribuidos-combustao', '/servicos-concluidos-combustao'];
  const allowedRoutes2 = [ '/','/logOut','/detalhes-servico/','/detalhes-servicoConcluido/','/servicos-eletricos', '/servicos-atribuidos-eletricos', '/servicos-concluidos-eletricos'];
  const allowedRoutes3 = [ '/','/logOut','/detalhes-servico/','/detalhes-servicoConcluido/','/servicos-hibridos', '/servicos-atribuidos-hibridos', '/servicos-concluidos-hibridos'];
  const allowedRoutes4 = [ '/','/logOut','/detalhes-servico/','/detalhes-servicoConcluido/','/servicos-gerais', '/servicos-atribuidos', '/servicos-concluidos'];
  // Importe o localStorage
  const loggedInUserPosto = localStorage.getItem('loggedInUserPosto');

  if (!loggedInUserPosto && to.path === '/' && (from.path === '/' || from.path === '/logOut')) {
    next();
  } else {
    switch (loggedInUserPosto) {
      case '1':
        if (verificaPrefixo(allowedRoutes1,to.path)) {
          next();
        } else {
          next('/');
        }
        break;
      case '2':
        if (verificaPrefixo(allowedRoutes2,to.path)) {
          next();
        } else {
          next('/');
        }
        break;
      case '3':
        if (verificaPrefixo(allowedRoutes3,to.path)) {
          next();
        } else {
          next('/');
        }
        break;
      case '4':
        if (verificaPrefixo(allowedRoutes4,to.path)) {
          next();
        } else {
          next('/');
        }
        break;
      default:
        next('/');
    }
  }
});


export default router;