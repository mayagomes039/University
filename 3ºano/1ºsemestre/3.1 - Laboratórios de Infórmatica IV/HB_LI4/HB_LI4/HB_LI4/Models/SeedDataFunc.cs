using HB_LI4.Data;

namespace HB_LI4.Models;
using Microsoft.EntityFrameworkCore;

public class SeedDataFunc
{
   
    public static void Initialize(IServiceProvider serviceProvider)
    {
        using (var context = new HB_LI4DbContext(
                   serviceProvider.GetRequiredService<
                       DbContextOptions<HB_LI4DbContext>>()))
        {
            if (context.Funcionarios.Any())
            {
                return;  
            }

            context.Funcionarios.AddRange(
                new Funcionario()
                {
                    Id = "1", 
                    Nome = "José Alberto",
                    PalavraPasse = "souOjose",
                    Email = "jose.alberto@gmail.com",
                    telemovel = 963456788
                }
            );
            context.SaveChanges();
        }
    }
}
