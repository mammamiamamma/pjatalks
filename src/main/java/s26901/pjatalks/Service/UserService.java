package s26901.pjatalks.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s26901.pjatalks.Constraints.ObjectIdValidation;
import s26901.pjatalks.DTO.Input.RoleDto;
import s26901.pjatalks.DTO.Input.UserInputDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.DTO.View.PostViewDto;
import s26901.pjatalks.DTO.View.UserViewDto;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Entity.UserRole;
import s26901.pjatalks.Exception.NotAcknowledgedException;
import s26901.pjatalks.Exception.RoleNotFoundException;
import s26901.pjatalks.Mapper.PostMapper;
import s26901.pjatalks.Mapper.UserMapper;
import s26901.pjatalks.Repository.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final FollowingRepository followingRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final RolesRepository rolesRepository;
    private final NotificationRepository notificationRepository;
    private final Validator validator;

    public UserService(PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       PostRepository postRepository,
                       LikeRepository likeRepository,
                       CommentRepository commentRepository,
                       UserMapper userMapper,
                       PostMapper postMapper,
                       FollowingRepository followingRepository,
                       RolesRepository rolesRepository,
                       NotificationRepository notificationRepository,
                       Validator validator) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.followingRepository = followingRepository;
        this.rolesRepository = rolesRepository;
        this.notificationRepository = notificationRepository;
        this.validator = validator;
    }

    private void validateUserUniqueness(String email, String username) {
        if (userRepository.findUserByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email provided is taken");
        }
        if (userRepository.findUserByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username provided is taken");
        }
    }

    public UserViewDto getUserForView(String id, int page, int size) {
        UserOutputDto userOutputDto = userRepository.findById(new ObjectId(id))
                .map(userMapper::map)
                .orElseThrow();

        PageRequest pageRequest = PageRequest.of(page, size);
        List<PostViewDto> postsByUser = postRepository.findByUserIdPaged(new ObjectId(userOutputDto.getId()), pageRequest)
                .stream()
                .map(post -> {
                    PostViewDto postViewDto = new PostViewDto();
                    postViewDto.setPost_id(post.getId());
                    postViewDto.setUser(userOutputDto);
                    postViewDto.setPost(postMapper.map(post));
                    postViewDto.setLikeCount(likeRepository.countLikesByPostId(new ObjectId(post.getId())));
                    postViewDto.setCommentCount(commentRepository.countCommentsByPost(new ObjectId(post.getId())));
                    postViewDto.setAlreadyLikedByUser(likeRepository.isAlreadyLiked(new ObjectId(userOutputDto.getId()), new ObjectId(post.getId())));
                    return postViewDto;
                })
                .collect(Collectors.toList());

        UserViewDto userViewDto = new UserViewDto();
        userViewDto.setUser(userOutputDto);
        userViewDto.setPosts(postsByUser);
        userViewDto.setFollowerCounter(followingRepository.getFollowerCountForUser(new ObjectId(userOutputDto.getId())));
        userViewDto.setFollowingCounter(followingRepository.getFollowingCountForUser(new ObjectId(userOutputDto.getId())));
        return userViewDto;
    }
    public UserOutputDto getUserById(String id) {
        return userRepository.findById(new ObjectId(id)).map(userMapper::map).orElse(null);
    }

    public String insertUser(UserInputDto userInputDto) throws RoleNotFoundException {
        validateUserUniqueness(userInputDto.getEmailAddress(), userInputDto.getUsername());
        return userRepository.insertUser(userMapper.map(userInputDto));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        user.getAuthorities()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public void registerUser(UserInputDto userInputDto) throws RoleNotFoundException {
        validateUserUniqueness(userInputDto.getEmailAddress(), userInputDto.getUsername());
        userInputDto.setUserRoles(Collections.singleton(new RoleDto("USER")));
        userInputDto.setPassword(passwordEncoder.encode(userInputDto.getPassword()));
        User user = userMapper.map(userInputDto);
        user.setLastCheckedNotifications(Date.from(Instant.now()));
        userRepository.insertUser(user);
    }

//    public boolean updateLastVisited(String user_id, Date visitedTime){
//        return userRepository.updateUsersLastVisitedTime(new ObjectId(user_id), visitedTime);
//    }

//    public Authentication authenticate(LoginInput loginInput) throws AuthenticationException {
//        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginInput.getUsername(), loginInput.getPassword()));
//    }

    public boolean addRoleToUser(String user_id, String role_name) throws RoleNotFoundException{
        UserRole userRole = rolesRepository.findByName(role_name);
        if (userRole == null) throw new RoleNotFoundException("No such role found!");
        User user = userRepository.findById(new ObjectId(user_id)).orElseThrow();
        user.addRole(userRole);
        return userRepository.updateUser(new ObjectId(user_id), user);
    }

    public boolean deleteRoleFromUser(String user_id, String role_name) throws RoleNotFoundException{
        UserRole userRole = rolesRepository.findByName(role_name);
        if (userRole == null) throw new RoleNotFoundException("No such role found!");
        User user = userRepository.findById(new ObjectId(user_id)).orElseThrow();
        user.deleteRoleByName(userRole.getName());
        return userRepository.updateUser(new ObjectId(user_id), user);
    }

    public Optional<UserOutputDto> findByUsername(String username){
        return userRepository.findUserByUsername(username).map(userMapper::map);
    }
    @Transactional
    public boolean deleteUser(@ObjectIdValidation String userId) throws NotAcknowledgedException {
        ObjectId userObjectId = new ObjectId(userId);
        boolean acknowledged = commentRepository.deleteCommentsByUser(userObjectId) &&
                followingRepository.deleteAllFollowersForUser(userObjectId) &&
                followingRepository.deleteAllFollowingForUser(userObjectId) &&
                likeRepository.deleteAllLikesByUser(userObjectId) &&
                notificationRepository.deleteNotificationsOfUser(userObjectId) &&
                notificationRepository.deleteNotificationsByUser(userObjectId) &&
                userRepository.deleteUser(userObjectId);
        if (!acknowledged) {
            throw new NotAcknowledgedException("Deletion not acknowledged by database");
        }
        return true;
    }

    @Transactional
    public boolean updateUser(String id, User updatedUser) {
        Optional<User> userWithEmail = userRepository.findUserByEmail(updatedUser.getEmailAddress());
        Optional<User> userWithUsername = userRepository.findUserByUsername(updatedUser.getUsername());
        if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("Email provided is taken");
        }
        if (userWithUsername.isPresent() && !userWithUsername.get().getId().equals(id)) {
            throw new IllegalArgumentException("Username provided is taken");
        }
        User existingUser = userRepository.findById(new ObjectId(id)).orElseThrow();
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setEmailAddress(updatedUser.getEmailAddress());
        existingUser.setShortBio(updatedUser.getShortBio());
        existingUser.setRoles(updatedUser.getRoles());
        return userRepository.updateUser(new ObjectId(id), existingUser);
    }

    @Transactional
    public void updateShortBio(String userId, String shortBio) throws ConstraintViolationException, NoSuchElementException {
        User user = userRepository.findById(new ObjectId(userId)).orElseThrow();
        user.setShortBio(shortBio);
        Set<ConstraintViolation<User>> errors = validator.validateProperty(user, "shortBio");
        if (!errors.isEmpty()) throw new ConstraintViolationException(errors);
        userRepository.updateUser(new ObjectId(userId), user);
    }

    public void updateLastCheckedNotifications(String userId) {
        userRepository.findById(new ObjectId(userId)).ifPresent(user -> {
            user.setLastCheckedNotifications(Date.from(Instant.now()));
            userRepository.updateUser(new ObjectId(userId), user);
        });
    }


    public boolean hasNewNotifications(String userId) {
        return userRepository.findById(new ObjectId(userId))
                .map(user -> notificationRepository.existsByUserIdAndTimestampAfter(
                        new ObjectId(userId), user.getLastCheckedNotifications())
                )
                .orElse(false);
    }

    public List<UserOutputDto> findTop3SuggestedUsers(UserOutputDto loggedInUser) {
        List<String> followingIds = followingRepository.getListOfFollowingIds(new ObjectId(loggedInUser.getId()));

        if (followingIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> followingUsers = userRepository.findUsers(
                followingIds.stream().map(ObjectId::new).collect(Collectors.toList())
        );
        Map<String, Integer> suggestionCount = new HashMap<>();

        for (User fUser : followingUsers) {
            if (fUser == null) {
                continue;
            }
            List<String> followers = followingRepository.getListOfFollowingIds(new ObjectId(fUser.getId()));
            for (String followerId : followers) {
                if (followerId == null || followerId.equals(loggedInUser.getId()) || followingIds.contains(followerId)) {
                    continue;
                }
                suggestionCount.put(followerId, suggestionCount.getOrDefault(followerId, 0) + 1);
            }
        }

        List<Map.Entry<String, Integer>> sortedSuggestions = suggestionCount.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .toList();

        List<String> top3SuggestedUserIds = sortedSuggestions.stream()
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
        List<UserOutputDto> listOfUsers = new ArrayList<>();
        userRepository.findUsers(top3SuggestedUserIds
                .stream().map(ObjectId::new)
                .collect(Collectors.toList())).forEach(user -> {
            if (user!=null) listOfUsers.add(userMapper.map(user));
        });
        return listOfUsers;
    }

    public List<String> getFollowedUserIds(String id) {
        return followingRepository.getListOfFollowingIds(new ObjectId(id));
    }
}
