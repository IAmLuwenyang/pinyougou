package com.pinyougou.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TbAreas implements Serializable {

    private static final long serialVersionUID = -5918374704662956477L;

    private Integer id;

    private String areaid;

    private String area;

    private String cityid;
}