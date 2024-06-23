using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using HB_LI4.Data;
using HB_LI4.Models;

namespace HB_LI4.Controllers
{
    public class CategoriaController : Controller
    {
        private readonly HB_LI4DbContext _context;

        public CategoriaController(HB_LI4DbContext context)
        {
            _context = context;
        }

        // GET: Categoria
        
        public async Task<IActionResult> Index()
        {
            if (HttpContext.Session.GetString("Autorizado") != null)
            {
                string TipoUser = HttpContext.Session.GetString("TipoUser");
                
                if (TipoUser == "Funcionario")
                {
                    return _context.Categorias != null ? 
                        View(await _context.Categorias.ToListAsync()) :
                        Problem("Entity set 'HB_LI4Context2.Categoria'  is null.");
                }
            }
            return RedirectToAction("Logout", "MenuCliente");
        }
        /*
        public async Task<IActionResult> Index()
        {
              return _context.Categorias != null ? 
                          View(await _context.Categorias.ToListAsync()) :
                          Problem("Entity set 'HB_LI4Context2.Categoria'  is null.");
        }
        */

        // GET: Categoria/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (id == null || _context.Categorias == null)
                {
                    return NotFound();
                }

                var categoria = await _context.Categorias
                    .FirstOrDefaultAsync(m => m.ID == id);
                if (categoria == null)
                {
                    return NotFound();
                }

                return View(categoria);
            }

            return RedirectToAction("Logout", "MenuCliente");
        }

        // GET: Categoria/Create
        public IActionResult Create()
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                return View();
            }

            return RedirectToAction("Logout", "MenuCliente");
        }

        // POST: Categoria/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("ID,Nome")] Categoria categoria)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (ModelState.IsValid)
                {
                    _context.Add(categoria);
                    await _context.SaveChangesAsync();
                    return RedirectToAction(nameof(Index));
                }

                return View(categoria);
            }

            return RedirectToAction("Logout", "MenuCliente");
        }

        // GET: Categoria/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (id == null || _context.Categorias == null)
                {
                    return NotFound();
                }

                var categoria = await _context.Categorias.FindAsync(id);
                if (categoria == null)
                {
                    return NotFound();
                }

                return View(categoria);
            }

            return RedirectToAction("Logout", "MenuCliente");
        }

        // POST: Categoria/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("ID,Nome")] Categoria categoria)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (id != categoria.ID)
                {
                    return NotFound();
                }

                if (ModelState.IsValid)
                {
                    try
                    {
                        _context.Update(categoria);
                        await _context.SaveChangesAsync();
                    }
                    catch (DbUpdateConcurrencyException)
                    {
                        if (!CategoriaExists(categoria.ID))
                        {
                            return NotFound();
                        }
                        else
                        {
                            throw;
                        }
                    }

                    return RedirectToAction(nameof(Index));
                }

                return View(categoria);
            }

            return RedirectToAction("Logout", "MenuCliente");
        }

        // GET: Categoria/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (id == null || _context.Categorias == null)
                {
                    return NotFound();
                }

                var categoria = await _context.Categorias
                    .FirstOrDefaultAsync(m => m.ID == id);
                if (categoria == null)
                {
                    return NotFound();
                }

                return View(categoria);
            }

            return RedirectToAction("Logout", "MenuCliente");
        }

        // POST: Categoria/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (_context.Categorias == null)
                {
                    return Problem("Entity set 'HB_LI4Context2.Categoria'  is null.");
                }

                var categoria = await _context.Categorias.FindAsync(id);
                if (categoria != null)
                {
                    _context.Categorias.Remove(categoria);
                }

                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            return RedirectToAction("Logout", "MenuCliente");
        }

        private bool CategoriaExists(int id)
        {
          return (_context.Categorias?.Any(e => e.ID == id)).GetValueOrDefault();
        }
    }
}
