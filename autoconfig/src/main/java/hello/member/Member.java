package hello.member;

import lombok.Data;

@Data
public class Member {

    private String name;
    private String memberId;

    public Member() {
    }

    public Member(String name, String memberId) {
        this.name = name;
        this.memberId = memberId;
    }
}
