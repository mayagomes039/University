using HB_LI4.Data;

namespace HB_LI4.Models;
using Microsoft.EntityFrameworkCore;

public class SeedDataCateg
{
   
    public static void Initialize(IServiceProvider serviceProvider)
    {
        using (var context = new HB_LI4DbContext(
                   serviceProvider.GetRequiredService<
                       DbContextOptions<HB_LI4DbContext>>()))
        {
            // Look for any Categoria.
            if (context.Categorias.Any())
            {
                return;   // DB has been seeded
            }

            context.Categorias.AddRange(
                new Categoria()
                {
                    Nome = "Loiça",
                },
                new Categoria()
                {
                    Nome = "Objetos de coleção"
                },
                new Categoria()
                {
                    Nome = "Joalharia"
                },
                new Categoria()
                {
                    Nome= "Brinquedos"
                },
                new Categoria()
                {
                    Nome = "Arte"
                },
                new Categoria()
                {
                    Nome = "Tapeçarias"
                }
              
            );
            context.SaveChanges();
        }
    }
}