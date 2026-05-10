using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class AddSafeZoneToDog : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<double>(
                name: "SafeRadius",
                table: "Dogs",
                type: "REAL",
                nullable: true);

            migrationBuilder.AddColumn<double>(
                name: "SafeZoneLatitude",
                table: "Dogs",
                type: "REAL",
                nullable: true);

            migrationBuilder.AddColumn<double>(
                name: "SafeZoneLongitude",
                table: "Dogs",
                type: "REAL",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "SafeRadius",
                table: "Dogs");

            migrationBuilder.DropColumn(
                name: "SafeZoneLatitude",
                table: "Dogs");

            migrationBuilder.DropColumn(
                name: "SafeZoneLongitude",
                table: "Dogs");
        }
    }
}
