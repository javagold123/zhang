package com.itlichao.backendlabor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itlichao.backendlabor.entity.ReservationGroup;
import com.itlichao.backendlabor.entity.ReservationGroupMember;
import com.itlichao.backendlabor.entity.SysUser;
import com.itlichao.backendlabor.mapper.ReservationGroupMapper;
import com.itlichao.backendlabor.mapper.ReservationGroupMemberMapper;
import com.itlichao.backendlabor.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamReservationService {

    private final ReservationGroupMapper groupMapper;
    private final ReservationGroupMemberMapper memberMapper;
    private final SysUserMapper userMapper;

    public TeamReservationService(ReservationGroupMapper groupMapper, ReservationGroupMemberMapper memberMapper, SysUserMapper userMapper) {
        this.groupMapper = groupMapper;
        this.memberMapper = memberMapper;
        this.userMapper = userMapper;
    }

    public List<Map<String, Object>> myGroups(Long userId) {
        // groups where user is a member
        List<ReservationGroupMember> memberships = memberMapper.selectList(
                new LambdaQueryWrapper<ReservationGroupMember>().eq(ReservationGroupMember::getUserId, userId));
        List<Long> groupIds = memberships.stream().map(ReservationGroupMember::getGroupId).distinct().collect(Collectors.toList());
        if (groupIds.isEmpty()) return List.of();
        List<ReservationGroup> groups = groupMapper.selectList(new LambdaQueryWrapper<ReservationGroup>().in(ReservationGroup::getId, groupIds));
        Map<Long, String> roleByGroup = memberships.stream().collect(Collectors.toMap(ReservationGroupMember::getGroupId, ReservationGroupMember::getRole, (a, b) -> a));
        return groups.stream().map(g -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", g.getId());
            m.put("name", g.getName());
            m.put("ownerId", g.getOwnerId());
            m.put("role", roleByGroup.getOrDefault(g.getId(), "member"));
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> members(Long groupId, Long userId) {
        assertMember(groupId, userId);
        List<ReservationGroupMember> members = memberMapper.selectList(new LambdaQueryWrapper<ReservationGroupMember>().eq(ReservationGroupMember::getGroupId, groupId));
        return members.stream().map(mm -> {
            SysUser u = userMapper.selectById(mm.getUserId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", mm.getId());
            m.put("userId", mm.getUserId());
            m.put("name", u != null ? u.getName() : "");
            m.put("username", u != null ? u.getUsername() : "");
            m.put("role", mm.getRole());
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ReservationGroup createGroup(Long ownerId, String name) {
        if (name == null || name.isBlank()) throw new RuntimeException("团队名称不能为空");
        ReservationGroup g = new ReservationGroup();
        g.setName(name.trim());
        g.setOwnerId(ownerId);
        g.setCreatedAt(LocalDateTime.now());
        g.setUpdatedAt(LocalDateTime.now());
        groupMapper.insert(g);

        ReservationGroupMember m = new ReservationGroupMember();
        m.setGroupId(g.getId());
        m.setUserId(ownerId);
        m.setRole("owner");
        m.setCreatedAt(LocalDateTime.now());
        memberMapper.insert(m);
        return g;
    }

    @Transactional
    public void renameGroup(Long groupId, Long userId, String name) {
        ReservationGroup g = groupMapper.selectById(groupId);
        if (g == null) throw new RuntimeException("团队不存在");
        if (!userId.equals(g.getOwnerId())) throw new RuntimeException("仅创建者可修改");
        if (name == null || name.isBlank()) throw new RuntimeException("团队名称不能为空");
        g.setName(name.trim());
        g.setUpdatedAt(LocalDateTime.now());
        groupMapper.updateById(g);
    }

    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        ReservationGroup g = groupMapper.selectById(groupId);
        if (g == null) return;
        if (!userId.equals(g.getOwnerId())) throw new RuntimeException("仅创建者可删除");
        groupMapper.deleteById(groupId);
    }

    @Transactional
    public void addMemberByUsername(Long groupId, Long ownerId, String username) {
        ReservationGroup g = groupMapper.selectById(groupId);
        if (g == null) throw new RuntimeException("团队不存在");
        if (!ownerId.equals(g.getOwnerId())) throw new RuntimeException("仅创建者可添加成员");
        if (username == null || username.isBlank()) throw new RuntimeException("用户名不能为空");
        SysUser u = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username.trim()));
        if (u == null) throw new RuntimeException("用户不存在");
        Long cnt = memberMapper.selectCount(new LambdaQueryWrapper<ReservationGroupMember>()
                .eq(ReservationGroupMember::getGroupId, groupId).eq(ReservationGroupMember::getUserId, u.getId()));
        if (cnt != null && cnt > 0) return;
        ReservationGroupMember m = new ReservationGroupMember();
        m.setGroupId(groupId);
        m.setUserId(u.getId());
        m.setRole("member");
        m.setCreatedAt(LocalDateTime.now());
        memberMapper.insert(m);
    }

    @Transactional
    public void removeMember(Long groupId, Long ownerId, Long memberUserId) {
        ReservationGroup g = groupMapper.selectById(groupId);
        if (g == null) throw new RuntimeException("团队不存在");
        if (!ownerId.equals(g.getOwnerId())) throw new RuntimeException("仅创建者可移除成员");
        if (memberUserId != null && memberUserId.equals(g.getOwnerId())) throw new RuntimeException("不能移除创建者");
        memberMapper.delete(new LambdaQueryWrapper<ReservationGroupMember>()
                .eq(ReservationGroupMember::getGroupId, groupId)
                .eq(ReservationGroupMember::getUserId, memberUserId));
    }

    private void assertMember(Long groupId, Long userId) {
        Long cnt = memberMapper.selectCount(new LambdaQueryWrapper<ReservationGroupMember>()
                .eq(ReservationGroupMember::getGroupId, groupId).eq(ReservationGroupMember::getUserId, userId));
        if (cnt == null || cnt <= 0) throw new RuntimeException("无权限");
    }
}

