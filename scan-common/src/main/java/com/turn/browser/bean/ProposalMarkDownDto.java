package com.turn.browser.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @Description: Proposal githubMarkdown file entity class
 */
@Data
public class ProposalMarkDownDto {
    /**
     *Proposal status
     */
    @JSONField(name = "Status")
    private String status;

    /**
     *Proposal type
     */
    @JSONField(name = "Type")
    private String type;

    /**
     *Proposal Category
     */
    @JSONField(name = "Category")
    private String category;

    /**
     *Proposal description
     */
    @JSONField(name = "Description")
    private String description;

    /**
     * Proposal pIDID
     */
    @JSONField(name = "PIP")
    private String pIP;

    /**
     * Proposal initiator
     */
    @JSONField(name = "Author")
    private String author;

    /**
     * Proposal topic
     */
    @JSONField(name = "Topic")
    private String topic;

    /**
     * Proposal creation time
     */
    @JSONField(name = "Created")
    private String created;

}