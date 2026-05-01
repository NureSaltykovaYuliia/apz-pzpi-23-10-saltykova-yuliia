using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Entities.Models;

namespace Application.Abstractions.Interfaces
{
    public interface IUserRepository
    {
        Task<User> GetUserByEmailAsync(string email);
        Task<User> GetUserByIdAsync(int id);
        Task<bool> DoesUserExistAsync(string email);
        Task<User> AddUserAsync(User user);
        Task<User> UpdateUserAsync(User user);
        Task<bool> DeleteUserAsync(int id);
        Task<List<User>> GetAllUsersAsync();
        Task UpdateLastActivityAsync(int userId, DateTime lastActivity);
        
        // Friendship
        Task<bool> AddFriendAsync(int userId, int friendId);
        Task<bool> RemoveFriendAsync(int userId, int friendId);
        Task<List<User>> GetFriendsAsync(int userId);
        Task<bool> IsFriendAsync(int userId, int friendId);
    }
}
