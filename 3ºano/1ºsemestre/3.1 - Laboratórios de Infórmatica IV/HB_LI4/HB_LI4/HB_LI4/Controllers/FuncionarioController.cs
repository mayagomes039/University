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
    public class FuncionarioController : Controller
    {
        private  HB_LI4DbContext _context;

        public FuncionarioController(HB_LI4DbContext context)
        {
            _context = context;
        }

        // GET: Funcionario
        public IActionResult Index()
        {
            if (HttpContext.Session.GetString("Autorizado") != null)
            {
                string TipoUser = HttpContext.Session.GetString("TipoUser");

                if (TipoUser == "Funcionario")
                {
                    return View();
                }
            }
            return RedirectToAction("Logout","MenuCliente");
        }

        // GET: Funcionario/Details/5
        public async Task<IActionResult> Details(string? id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (id == null || _context.Funcionarios == null)
                {
                    return NotFound();
                }

                var funcionario = await _context.Funcionarios
                    .FirstOrDefaultAsync(m => m.Id == id);
                if (funcionario == null)
                {
                    return NotFound();
                }

                return View(funcionario);
            }

            return RedirectToAction("Logout","MenuCliente");
        }

        // GET: Funcionario/Create
        public IActionResult Create()
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
            return View();
            }
            return RedirectToAction("Logout","MenuCliente");
        }

        // POST: Funcionario/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("Id,Nome,PalavraPasse,Email,telemovel")] Funcionario funcionario)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (ModelState.IsValid)
                {
                    _context.Add(funcionario);
                    await _context.SaveChangesAsync();
                    return RedirectToAction(nameof(Index));
                }

                return View(funcionario);
            }
            return RedirectToAction("Logout","MenuCliente");
        }

            // GET: Funcionario/Edit/5
        public async Task<IActionResult> Edit(string? id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (id == null || _context.Funcionarios == null)
                {
                    return NotFound();
                }

                var funcionario = await _context.Funcionarios.FindAsync(id);
                if (funcionario == null)
                {
                    return NotFound();
                }

                return View(funcionario);
            }

            return RedirectToAction("Logout","MenuCliente");
        }

        // POST: Funcionario/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(string id, [Bind("Id,Nome,PalavraPasse,Email,telemovel")] Funcionario funcionario)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (id != funcionario.Id)
                {
                    return NotFound();
                }

                if (ModelState.IsValid)
                {
                    try
                    {
                        _context.Update(funcionario);
                        await _context.SaveChangesAsync();
                    }
                    catch (DbUpdateConcurrencyException)
                    {
                        if (!FuncionarioExists(funcionario.Id))
                        {
                            return NotFound();
                        }
                        else
                        {
                            throw;
                        }
                    }

                    return RedirectToAction("Edit", "Funcionario");
                }

                return View(funcionario);
            }
            return RedirectToAction("Logout","MenuCliente");
        }

            // GET: Funcionario/Delete/5
        public async Task<IActionResult> Delete(string? id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
            if (id == null || _context.Funcionarios == null)
            {
                return NotFound();
            }

            var funcionario = await _context.Funcionarios
                .FirstOrDefaultAsync(m => m.Id == id);
            if (funcionario == null)
            {
                return NotFound();
            }

            return View(funcionario);
        }
            return RedirectToAction("Logout","MenuCliente");
        }

        // POST: Funcionario/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(string id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
            if (_context.Funcionarios == null)
            {
                return Problem("Entity set 'HB_LI4Context1.Funcionario'  is null.");
            }
            var funcionario = await _context.Funcionarios.FindAsync(id);
            if (funcionario != null)
            {
                _context.Funcionarios.Remove(funcionario);
            }
            
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }
            return RedirectToAction("Logout","MenuCliente");
        }

        private bool FuncionarioExists(string id)
        {
          return (_context.Funcionarios?.Any(e => e.Id == id)).GetValueOrDefault();
        }
    }
}
