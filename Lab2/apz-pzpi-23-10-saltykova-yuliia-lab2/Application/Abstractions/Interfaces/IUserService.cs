using Application.DTOs;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Application.Abstractions.Interfaces
{
    public interface IUserService
    {
        // Профільні операції для користувача
        Task<UserProfileDto> GetMyProfileAsync(int userId);
        Task<UserProfileDto> UpdateMyProfileAsync(int userId, UpdateUserProfileDto updateDto);
        Task<bool> DeleteMyProfileAsync(int userId);
        Task<IEnumerable<UserProfileDto>> SearchUsersAsync(string? query, double? lat, double? lon, double? radius, int currentUserId);

        // Адміністративні операції
        Task<UserStatisticsDto> GetUserStatisticsAsync();
        Task<List<UserActivityDto>> GetAllUsersActivityAsync();
        Task<UserActivityDto> GetUserActivityAsync(int userId);
        Task<bool> BlockUserAsync(int userId, string blockReason);
        Task<bool> UnblockUserAsync(int userId);

        // Friendship
        Task<bool> AddFriendAsync(int userId, int friendId);
        Task<bool> RemoveFriendAsync(int userId, int friendId);
        Task<IEnumerable<UserProfileDto>> GetFriendsAsync(int userId);
    }
}
