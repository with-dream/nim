package user;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import netty.model.GroupMember;

import java.util.List;

public class GroupModel {
    public long groupId;
    public long userId;
    public String members;
    public String groupName;

    public List<GroupMember> getMembers(Gson gson) {
        return gson.fromJson(members, new TypeToken<List<GroupMember>>() {
        }.getType());
    }

    public String memToStr(Gson gson, List<GroupMember> mem) {
        return gson.toJson(mem);
    }

    @Override
    public String toString() {
        return "GroupModel{" +
                "groupId=" + groupId +
                ", userId=" + userId +
                ", members='" + members + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
