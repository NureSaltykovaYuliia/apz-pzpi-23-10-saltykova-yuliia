using Application.Abstractions.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using System.Threading.Tasks;

namespace MyDogSpace.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class ConversationsController : ControllerBase
    {
        private readonly IConversationService _conversationService;

        public ConversationsController(IConversationService conversationService)
        {
            _conversationService = conversationService;
        }

        private int GetCurrentUserId() => int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier));

        [HttpGet]
        public async Task<IActionResult> GetMyConversations()
        {
            var conversations = await _conversationService.GetUserConversationsAsync(GetCurrentUserId());
            return Ok(conversations);
        }

        [HttpGet("{id}/messages")]
        public async Task<IActionResult> GetMessageHistory(int id)
        {
            var messages = await _conversationService.GetMessageHistoryAsync(id, count: 50);
            return Ok(messages);
        }

        [HttpPost("private/{targetUserId}")]
        public async Task<IActionResult> CreatePrivateConversation(int targetUserId)
        {
            var conversation = await _conversationService.GetOrCreatePrivateConversationAsync(GetCurrentUserId(), targetUserId);
            return Ok(conversation);
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteConversation(int id)
        {
            var result = await _conversationService.DeleteConversationAsync(id, GetCurrentUserId());
            if (result) return Ok(new { message = "Розмову видалено." });
            return BadRequest(new { message = "Не вдалося видалити розмову." });
        }

        [HttpPost("{id}/messages")]
        public async Task<IActionResult> SendMessage(int id, [FromBody] string content)
        {
            var message = await _conversationService.SendMessageAsync(id, GetCurrentUserId(), content);
            return Ok(message);
        }
    }
}