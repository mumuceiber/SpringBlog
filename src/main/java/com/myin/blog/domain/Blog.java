package com.myin.blog.domain;

import com.github.rjeschke.txtmark.Processor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.security.Timestamp;

import lombok.Data;
import lombok.NoArgsConstructor;

//@Document(indexName = "blog", type = "blog")
@Entity
@Data
@NoArgsConstructor
public class Blog implements Serializable {

    private static final long serialVersionUid = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @NotEmpty(message = "Title cannot be empty")
    @Size(min=2, max=50)
    @Column(nullable = false, length = 50)
    private String title;

    @NotEmpty(message = "Summary cannot be empty")
    @Size(min=2, max=300)
    @Column(nullable = false)
    private String summary;

    @Lob //  大对象，映射 MySQL 的 Long Text 类型
    @Basic(fetch = FetchType.LAZY)// 懒加载
    @NotEmpty(message = "Content cannot be empty")
    @Size(min = 2)
    @Column(nullable = false) // 映射为字段，值不能为空
    private String content;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @NotEmpty(message = "Content cannot be empty")
    @Size(min = 2)
    @Column(nullable = false)
    private String htmlContent; // 将 md 转为 html

    @OneToOne(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Column(nullable = false)
    @org.hibernate.annotations.CreationTimestamp  // 由数据库自动创建时间
    private Timestamp createTime;

    @Column(name="reading")
    private Long reading = 0L;  // 访问量、阅读量

    @Column(name="comments")
    private Long comments = 0L; // 评论量

    @Column(name="likes")
    private Long likes = 0L; // 点赞量

    public Blog(String title, String summary, String content) {
        this.title = title;
        this.summary = summary;
        this.content = content;
    }

    public void setContent(String content) {
        this.content = content;
        this.htmlContent = Processor.process(content);
    }


    @Override
    public String toString() {
        return String.format("Blog[id='%s', title='%s', summary='%s', content='%s']", id, title, summary, content);
    }

}
