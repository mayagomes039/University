
using HB_LI4.Data;
using HB_LI4.Models;
using Humanizer;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;

namespace HB_LI4.Controllers;

public class MenuClienteController : Controller

{
    
    private readonly HB_LI4DbContext _context;
    

    public MenuClienteController(HB_LI4DbContext context)
    {
        _context = context;
       
    }

    // get : /Menu
    public IActionResult Index()
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Cliente")
            {
                return View();
            }
        }
        return Logout();
    }

    
    public async Task<IActionResult> VisualizarLeiloes(string Categoria, decimal? PrecoMaximo)
    {
        string TipoUser = HttpContext.Session.GetString("TipoUser");
        if (TipoUser == "Cliente")
        {
            // Obter o ID da categoria correspondente ao nome
            int? categoriaID = null;
            if (!string.IsNullOrEmpty(Categoria))
            {
                Categoria categoriaSelecionada = await _context.Categorias
                    .FirstOrDefaultAsync(c => c.Nome == Categoria);

                if (categoriaSelecionada != null)
                {
                    categoriaID = categoriaSelecionada.ID;
                }
            }

            IQueryable<string> genreQuery = from m in _context.Categorias
                orderby m.ID
                select m.Nome;

            // Modificação para incluir a propriedade de navegação Categoria
            var leiloes = from m in _context.Leiloes.Include(l => l.Categoria)
                where m.DataFim >= DateTime.Today 
                select m;

            if (categoriaID.HasValue)
            {
                leiloes = leiloes.Where(x => x.CategoriaID == categoriaID.Value);
            }

            if (PrecoMaximo.HasValue)
            {
                leiloes = leiloes.Where(x => x.PrecoFinal <= PrecoMaximo.Value);
            }
            
            foreach (var leilao in leiloes)
            {
                leilao.Imagem = await _context.Leiloes
                    .Where(l => l.ID == leilao.ID)
                    .Select(l => l.Imagem)
                    .FirstOrDefaultAsync();
            }
            
            var leiloesCategorias = new LeilaoCategoriaViewModel
            {
                Categorias = new SelectList(await genreQuery.Distinct().ToListAsync()),
                Leiloes = await leiloes.ToListAsync(),
                PrecoMaximo = PrecoMaximo,
            };

            return View(leiloesCategorias);
        }
        else
        {
            return RedirectToAction("Index", "Login");
        }
    }

    
    // get : /Menu/ConsultarPerfil?id=1
    // get : /Menu/ConsultarPerfil?id=1
    public async Task<IActionResult> ConsultarPerfil([FromServices] HB_LI4DbContext context)
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
            string id = HttpContext.Session.GetString("ClienteId");
            var cliente = await context.Clientes.FirstOrDefaultAsync(c => c.Id == id);

            if (cliente == null)
            {
                return NotFound();
            }

            return RedirectToAction("Details", "Cliente", new { id = cliente.Id });
        }
        else
        {
            return RedirectToAction("Index", "Login");
        }
    }

    public async Task<IActionResult> HistoricoCompras()
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
            string clienteId = HttpContext.Session.GetString("ClienteId");
            
            var leiloesHistoricoCompras = await _context.Leiloes
                .Include(l => l.Lances)
                .Where(l => l.DataFim < DateTime.Today)
                .ToListAsync();

            var leiloesParaCliente = new List<Leilao>();
            foreach (var leilao in leiloesHistoricoCompras)
            {
                
                var lanceMaiorValor = leilao.Lances.OrderByDescending(l => l.Valor).FirstOrDefault();

                if (lanceMaiorValor != null && lanceMaiorValor.ClienteID == clienteId)
                {
                    
                    leilao.ClienteID = clienteId;
                    leiloesParaCliente.Add(leilao);

                    _context.Update(leilao);
                    await _context.SaveChangesAsync();
                }
            }

            foreach (var leilao in leiloesHistoricoCompras)
            {
                
                if (leilao.Lances != null && leilao.Lances.Any())
                {
                    var lanceVencedor = leilao.Lances.OrderByDescending(l => l.Valor).FirstOrDefault();
                    if ( lanceVencedor != null && leilao.ClienteID == clienteId && leilao.leilaoPago == false)
                    {
                        
                        bool mensagemEnviada = await _context.Mensagens
                            .AnyAsync(m => m.ClienteID == clienteId && m.LeilaoID == leilao.ID && m.mensEnviada);

                        if (!mensagemEnviada)
                        {
                        string mensagem = $"Ganhaste o leilão '{leilao.Nome}'! Quando desejares, procede ao pagamento dos '{leilao.PrecoFinal}'€.";
                        
                        
                        var cliente = await _context.Clientes
                            .Include(c => c.Mensagens)
                            .FirstOrDefaultAsync(c => c.Id == clienteId);

                        if (cliente != null)
                        {
                            Mensagem novaMensagem = new Mensagem
                            {
                                Conteudo = mensagem,
                                ClienteID = clienteId,
                                LeilaoID = leilao.ID,
                                mensEnviada = true
                            };

                            cliente.Mensagens.Add(novaMensagem);
                            leilao.Mensagens.Add(novaMensagem);

                            await _context.SaveChangesAsync();
                        }
                        }
                    }
                }
            }

            return View(leiloesParaCliente);
        }
        else
        {
            return RedirectToAction("Index", "Login");
        }
    }


    public IActionResult Logout()
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
            HttpContext.Session.Clear();
            return RedirectToAction("Index", "Login");
        }
        else
        {
            return RedirectToAction("Index", "Login");
        }
    }

    
    public async Task<IActionResult> VisualizarMensagens()
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
            string clienteId = HttpContext.Session.GetString("ClienteId");

            // Obter o cliente do banco de dados com as suas mensagens
            var cliente = await _context.Clientes
                .Include(c => c.Mensagens) 
                .FirstOrDefaultAsync(c => c.Id == clienteId);

            if (cliente != null)
            {
                return View(cliente.Mensagens);
            }
            else
            {
                return RedirectToAction("Index", "Login"); 
            }
        }
        else
        {
            return RedirectToAction("Index", "Login");
        }
    }

    public async Task<IActionResult> Pagar(int mensagemId)
    {
        var mensagem = await _context.Mensagens.FindAsync(mensagemId);
        if (mensagem != null)
        {
            var leilaoId = mensagem.LeilaoID; 
            HttpContext.Session.SetInt32("LeilaoId", leilaoId);
            return View(mensagemId);
        }
        else
        {
            return RedirectToAction("Index", "Home");
        }
    }
    
    [HttpPost]
    public async Task<IActionResult> ConfirmarPagamento(int mensagemId, string formaPagamento)
    {
        switch (formaPagamento)
        {
            case "PagarNaLoja":
                break;

            case "Multibanco":
                // Redireciona para a página de formulário Multibanco
                return RedirectToAction("FormularioMultibanco", new { mensagemId = mensagemId });

            case "MBWay":
                // Redireciona para a página de formulário MBWay
                return RedirectToAction("FormularioMBWay", new { mensagemId = mensagemId });

            default:
                // Forma de pagamento inválida, redireciona para as mensagens
                return RedirectToAction("VisualizarMensagens");
        }

        // Caso o pagamento seja bem-sucedido, redireciona para as mensagens
        return RedirectToAction("VisualizarMensagens");
    }
    
    public IActionResult FormularioMultibanco(int mensagemId)
    {
        ViewData["mensagemId"] = mensagemId;
        return View(mensagemId);
    }

    [HttpPost]
    public async Task<IActionResult> ProcessarFormularioMultibanco(int mensagemId, string proprietario, string dataValidade, string numeroCartao)
    {
        var mensagem = _context.Mensagens.Find(mensagemId);
        if (mensagem != null)
        {
            int? IDleilaoNullable = HttpContext.Session.GetInt32("LeilaoId");
            int IDleilao = IDleilaoNullable.HasValue ? IDleilaoNullable.Value : 0;
            

            var leilao = await _context.Leiloes
                .FirstOrDefaultAsync(c => c.ID == IDleilao);
            
            leilao.leilaoPago = true;
            _context.Mensagens.Remove(mensagem);
            _context.SaveChanges();
        }
        return RedirectToAction("VisualizarMensagens");
    }
    
    public IActionResult FormularioMBWay(int mensagemId)
    {
        ViewData["mensagemId"] = mensagemId;
        return View(mensagemId);
    }

    [HttpPost]
    public async Task<IActionResult> ProcessarFormularioMBWay(int mensagemId, string proprietario, string dataValidade, string numeroCartao)
    {
        var mensagem = _context.Mensagens.Find(mensagemId);
        if (mensagem != null)
        {
            int? IDleilaoNullable = HttpContext.Session.GetInt32("LeilaoId");
            int IDleilao = IDleilaoNullable.HasValue ? IDleilaoNullable.Value : 0;
            Console.WriteLine("id: "+ IDleilao);

            var leilao = await _context.Leiloes
                .FirstOrDefaultAsync(c => c.ID == IDleilao);

            leilao.leilaoPago = true;
            _context.Mensagens.Remove(mensagem);
            _context.SaveChanges();
        }
        return RedirectToAction("VisualizarMensagens");
    }

}
    
