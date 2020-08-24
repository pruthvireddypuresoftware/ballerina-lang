meta:
  id: bir
  file-extension: bir
  endian: be
  license: Apache 2.0
doc-ref: https://github.com/ballerina-platform/ballerina-lang/blob/master/docs/compiler/bir-spec.md
seq:
  - id: constant_pool
    type: constant_pool_set
  - id: module
    type: module
enums:
  type_tag_enum:
    1:
      id: type_tag_int
      doc: Basic type, 64-bit signed integers
    2: type_tag_byte
    3: type_tag_float
    4: type_tag_decimal
    5: type_tag_string
    6: type_tag_boolean
    7: type_tag_json
    8: type_tag_xml
    9: type_tag_table
    10: type_tag_nil
    11: type_tag_anydata
    12: type_tag_record
    13: type_tag_typedesc
    14: type_tag_stream
    15: type_tag_map
    16: type_tag_invokable
    17: type_tag_any
    18: type_tag_endpoint
    19: type_tag_array
    20: type_tag_union
    21: type_tag_intersection
    22: type_tag_package
    23: type_tag_none
    24: type_tag_void
    25: type_tag_xmlns
    26: type_tag_annotation
    27: type_tag_semantic_error
    28: type_tag_error
    29: type_tag_iterator
    30: type_tag_tuple
    31: type_tag_future
    32: type_tag_finite
    33: type_tag_object
    34: type_tag_service
    35: type_tag_byte_array
    36: type_tag_function_pointer
    37: type_tag_handle
    38: type_tag_readonly
    39: type_tag_signed32_int
    40: type_tag_signed16_int
    41: type_tag_signed8_int
    42: type_tag_unsigned32_int
    43: type_tag_unsigned16_int
    44: type_tag_unsigned8_int
    45: type_tag_char_string
    46: type_tag_xml_element
    47: type_tag_xml_pi
    48: type_tag_xml_comment
    49: type_tag_xml_text
    50: type_tag_never
    51: type_tag_null_set
    52: type_tag_parameterized_type
types:
  constant_pool_set:
    seq:
      - id: constant_pool_count
        type: s4
      - id: constant_pool_entries
        type: constant_pool_entry
        repeat: expr
        repeat-expr: constant_pool_count
  constant_pool_entry:
    seq:
      - id: tag
        type: u1
        enum: tag_enum
      - id: cp_info
        type:
          switch-on: tag
          cases:
            'tag_enum::cp_entry_integer': int_cp_info
            'tag_enum::cp_entry_float': float_cp_info
            'tag_enum::cp_entry_boolean': boolean_cp_info
            'tag_enum::cp_entry_string': string_cp_info
            'tag_enum::cp_entry_package': package_cp_info
            'tag_enum::cp_entry_byte': byte_cp_info
            'tag_enum::cp_entry_shape': shape_cp_info
    enums:
      tag_enum:
        1: cp_entry_integer
        2: cp_entry_float
        3: cp_entry_boolean
        4: cp_entry_string
        5: cp_entry_package
        6: cp_entry_byte
        7: cp_entry_shape
  int_cp_info:
    seq:
      - id: value
        type: s8
  float_cp_info:
    seq:
      - id: value
        type: f8
  boolean_cp_info:
    seq:
      - id: value
        type: u1
  string_cp_info:
    seq:
      - id: str_len
        type: s4
      - id: value
        type: str
        size: str_len
        encoding: UTF-8
  package_cp_info:
    seq:
      - id: org_index
        type: s4
      - id: name_index
        type: s4
      - id: version_index
        type: s4
  byte_cp_info:
    seq:
      - id: value
        type: s4
  shape_cp_info:
    seq:
      - id: shape_lenght
        type: s4
      - id: shape
        size: shape_lenght
        type: type_info
  type_info:
    seq:
      - id: type_tag
        type: s1
        enum: type_tag_enum
      - id: name_index
        type: s4
      - id: type_flag
        type: s4
      - id: type_special_flag
        type: s4
      - id: type_structure
        type:
          switch-on: type_tag
          cases:
            'type_tag_enum::type_tag_invokable': type_invokable
    instances:
      name_as_str:
        value: _root.constant_pool.constant_pool_entries[name_index].cp_info.as<string_cp_info>.value
  type_invokable:
    seq:
      - id: param_count
        type: s4
      - id: has_rest_type
        type: u1
      - id: return_type_index
        type: s4
  module:
    seq:
      - id: id_cp_index
        type: s4
      - id: import_count
        type: s4
      - id: imports
        type: package_cp_info
        repeat: expr
        repeat-expr: import_count
      - id: const_count
        type: s4
      - id: constants
        type: constant
        repeat: expr
        repeat-expr: const_count
      - id: type_definition_count
        type: s4
      - id: type_definitions
        type: type_definition
        repeat: expr
        repeat-expr: type_definition_count
      - id: golbal_var_count
        type: s4
      - id: golbal_vars
        type: golbal_var
        repeat: expr
        repeat-expr: golbal_var_count
      - id: type_definition_bodies_count
        type: s4
      - id: type_definition_bodies
        type: type_definition_body
        repeat: expr
        repeat-expr: type_definition_bodies_count
      - id: function_count
        type: s4
      - id: functions
        type: function
        repeat: expr
        repeat-expr: function_count
      - id: annotations_size
        type: s4
  golbal_var:
    seq:
      - id: kind
        type: s1
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: doc
        type: markdown
      - id: type_cp_index
        type: s4
  type_definition:
    seq:
      - id: position
        type: position
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: label
        type: s1
      - id: doc
        type: markdown
      - id: type_cp_index
        type: s4
  type_definition_body:
    seq:
      - id: attached_functions_count
        type: s4
      - id: attached_functions
        type: function
        repeat: expr
        repeat-expr: attached_functions_count
      - id: referenced_types_count
        type: s4
      - id: referenced_types
        type: referenced_type
        repeat: expr
        repeat-expr: referenced_types_count
  constant:
    seq:
      - id: name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: doc
        type: markdown
      - id: type_cp_index
        type: s4
      - id: length
        type: s8
      - id: constant_value
        type: constant_value
  constant_value:
    seq:
      - id: constant_value_type_cp_index
        type: s4
      - id: constant_value_info
        type:
          switch-on: type.shape.type_tag
          cases:
            'type_tag_enum::type_tag_int': int_constant_info
            'type_tag_enum::type_tag_byte': byte_constant_info
            'type_tag_enum::type_tag_float': float_constant_info
            'type_tag_enum::type_tag_string': string_constant_info
            'type_tag_enum::type_tag_decimal': decimal_constant_info
            'type_tag_enum::type_tag_boolean': boolean_constant_info
            'type_tag_enum::type_tag_nil': nil_constant_info
            'type_tag_enum::type_tag_map': map_constant_info
    instances:
      type:
        value: _root.constant_pool.constant_pool_entries[constant_value_type_cp_index].cp_info.as<shape_cp_info>
  int_constant_info:
    seq:
      - id: value_cp_index
        type: s4
  byte_constant_info:
    seq:
      - id: value_cp_index
        type: s4
  float_constant_info:
    seq:
      - id: value_cp_index
        type: s4
  string_constant_info:
    seq:
      - id: value_cp_index
        type: s4
  decimal_constant_info:
    seq:
      - id: value_cp_index
        type: s4
  boolean_constant_info:
    seq:
      - id: value_boolean_constant
        type: s2
  nil_constant_info:
    seq:
      - id: value_nil_constant
        size: 0
  map_constant_info:
    seq:
      - id: map_constant_size
        type: s4
      - id: map_key_values
        type: map_key_value
        repeat: expr
        repeat-expr: map_constant_size
  map_key_value:
    seq:
      - id: key_name_cp_index
        type: s4
      - id: key_value_info
        type: constant_value
  markdown:
    seq:
      - id: length
        type: s4
      - id: has_doc
        type: markdown_content
        size: length
  markdown_content:
    seq:
      - id: has_doc
        type: u1
  function:
    seq:
      - id: position
        type: position
      - id: name_cp_index
        type: s4
      - id: worker_name_cp_index
        type: s4
      - id: flags
        type: s4
      - id: type_cp_index
        type: s4
      - id: annotation_attachments_content_length
        type: s8
      - id: annotation_attachments
        size: annotation_attachments_content_length
      - id: required_param_count
        type: s4
      - id: required_params
        type: required_param
        repeat: expr
        repeat-expr: required_param_count
      - id: has_rest_param
        type: u1
      - id: rest_param_name_cp_index
        type: s4
        if: has_rest_param != 0
      - id: has_receiver
        type: u1
      - id: reciever
        type: reciever
        if: has_receiver != 0
      - id: taint_table_length
        type: s8
      - id: taint_table
        type: taint_table
        size: taint_table_length
      - id: doc
        type: markdown
      - id: function_body_length
        type: s8
      - id: function_body
        type: function_body
        size: function_body_length
  referenced_type:
    seq:
      - id: type_cp_index
        type: s4
  required_param:
    seq:
      - id: param_name_cp_index
        type: s4
      - id: flags
        type: s4
  reciever:
    seq:
      - id: kind
        type: s1
      - id: type_cp_index
        type: s4
      - id: name_cp_index
        type: s4
  taint_table:
    seq:
      - id: row_count
        type: s2
      - id: column_count
        type: s2
  function_body:
    seq:
      - id: args_count
        type: s4
      - id: has_return_var
        type: u1
      - id: return_var
        type: return_var
        if: has_return_var != 0
      - id: dafault_parameter_count
        type: s4
      - id: dafault_parameters
        type: dafault_parameter
        repeat: expr
        repeat-expr: dafault_parameter_count
      - id: local_variables_count
        type: s4
      - id: local_variables
        type: local_variable
        repeat: expr
        repeat-expr: local_variables_count
      - id: has_default_params_basic_blocks
        type: u1
      - id: default_parameter_basic_blocks_info
        type: basic_blocks_info
        if: has_default_params_basic_blocks != 0
      - id: function_basic_blocks_info
        type: basic_blocks_info
  return_var:
    seq:
      - id: kind
        type: s1
      - id: type_cp_index
        type: s4
      - id: name_cp_index
        type: s4
  dafault_parameter:
    seq:
      - id: kind
        type: s1
      - id: type_cp_index
        type: s4
      - id: name_cp_index
        type: s4
      - id: meta_var_name_cp_index
        type: s4
        if: kind == 2
      - id: has_default_expr
        type: u1
  local_variable:
    seq:
      - id: kind
        type: s1
      - id: type_cp_index
        type: s4
      - id: name_cp_index
        type: s4
      - id: meta_var_name_cp_index
        type: s4
        if: kind == 2
      - id: enclosing_basic_block_id
        type: enclosing_basic_block_id
        if: kind == 1
  enclosing_basic_block_id:
    seq:
      - id: meta_var_name_cp_index
        type: s4
      - id: end_bb_id_cp_index
        type: s4
      - id: start_bb_id_cp_index
        type: s4
      - id: instruction_offset
        type: s4
  position:
    seq:
      - id: s_line
        type: s4
      - id: e_line
        type: s4
      - id: s_col
        type: s4
      - id: e_col
        type: s4
      - id: source_file_cp_index
        type: s4
  basic_blocks_info:
    seq:
      - id: basic_blocks_count
        type: s4
      - id: basic_blocks
        type: basic_block
        repeat: expr
        repeat-expr: basic_blocks_count
  basic_block:
    seq:
      - id: name_cp_index
        type: s4
      - id: instructions_count
        type: s4
      - id: instructions
        type: instruction
        repeat: expr
        repeat-expr: instructions_count
  instruction:
    seq:
      - id: position
        type: position
      - id: instruction_kind
        type: s1
        enum: instruction_kind_enum
      - id: instruction_structure
        type:
          switch-on: instruction_kind
          cases:
            'instruction_kind_enum::instruction_kind_goto': instruction_goto
            'instruction_kind_enum::instruction_kind_return': instruction_return
            'instruction_kind_enum::instruction_kind_new_typedesc': instruction_new_typedesc
            'instruction_kind_enum::instruction_kind_new_structure': instruction_new_structure
            'instruction_kind_enum::instruction_kind_const_load': instruction_const_load
            'instruction_kind_enum::instruction_kind_move': instruction_move
    enums:
      instruction_kind_enum:
        1: instruction_kind_goto
        2: instruction_kind_call
        3: instruction_kind_branch
        4: instruction_kind_return
        5: instruction_kind_async_call
        6: instruction_kind_wait
        7: instruction_kind_fp_call
        8: instruction_kind_wk_receive
        9: instruction_kind_wk_send
        10: instruction_kind_flush
        11: instruction_kind_lock
        12: instruction_kind_field_lock
        13: instruction_kind_unlock
        14: instruction_kind_wait_all
        20: instruction_kind_move
        21: instruction_kind_const_load
        22: instruction_kind_new_structure
        23: instruction_kind_map_store
        24: instruction_kind_map_load
        25: instruction_kind_new_array
        26: instruction_kind_array_store
        27: instruction_kind_array_load
        28: instruction_kind_new_error
        29: instruction_kind_type_cast
        30: instruction_kind_is_like
        31: instruction_kind_type_test
        32: instruction_kind_new_instance
        33: instruction_kind_object_store
        34: instruction_kind_object_load
        35: instruction_kind_panic
        36: instruction_kind_fp_load
        37: instruction_kind_string_load
        38: instruction_kind_new_xml_element
        39: instruction_kind_new_xml_text
        40: instruction_kind_new_xml_comment
        41: instruction_kind_new_xml_pi
        42: instruction_kind_new_xml_seq
        43: instruction_kind_new_xml_qname
        44: instruction_kind_new_string_xml_qname
        45: instruction_kind_xml_seq_store
        46: instruction_kind_xml_seq_load
        47: instruction_kind_xml_load
        48: instruction_kind_xml_load_all
        49: instruction_kind_xml_attribute_load
        50: instruction_kind_xml_attribute_store
        51: instruction_kind_new_table
        52: instruction_kind_new_typedesc
        53: instruction_kind_new_stream
        54: instruction_kind_table_store
        55: instruction_kind_table_load
        61: instruction_kind_add
        62: instruction_kind_sub
        63: instruction_kind_mul
        64: instruction_kind_div
        65: instruction_kind_mod
        66: instruction_kind_equal
        67: instruction_kind_not_equal
        68: instruction_kind_greater_than
        69: instruction_kind_greater_equal
        70: instruction_kind_less_than
        71: instruction_kind_less_equal
        72: instruction_kind_and
        73: instruction_kind_or
        74: instruction_kind_ref_equal
        75: instruction_kind_ref_not_equal
        76: instruction_kind_closed_range
        77: instruction_kind_half_open_range
        78: instruction_kind_annot_access
        80: instruction_kind_typeof
        81: instruction_kind_not
        82: instruction_kind_negate
        83: instruction_kind_bitwise_and
        84: instruction_kind_bitwise_or
        85: instruction_kind_bitwise_xor
        86: instruction_kind_bitwise_left_shift
        87: instruction_kind_bitwise_right_shift
        88: instruction_kind_bitwise_unsigned_right_shift
        128: instruction_kind_platform
  instruction_const_load:
    seq:
      - id: type_cp_index
        type: s4
      - id: lhs_operand
        type: operand
      - id: constant_value_info
        type:
          switch-on: type.shape.type_tag
          cases:
            'type_tag_enum::type_tag_int': int_constant_info
            'type_tag_enum::type_tag_byte': byte_constant_info
            'type_tag_enum::type_tag_float': float_constant_info
            'type_tag_enum::type_tag_string': string_constant_info
            'type_tag_enum::type_tag_decimal': decimal_constant_info
            'type_tag_enum::type_tag_boolean': boolean_constant_info
            'type_tag_enum::type_tag_nil': nil_constant_info
    instances:
      type:
        value: _root.constant_pool.constant_pool_entries[type_cp_index].cp_info.as<shape_cp_info>
  instruction_goto:
    seq:
      - id: target_bb_id_name_cp_index
        type: s4
  instruction_move:
    seq:
      - id: rhs_operand
        type: operand
      - id: lhs_operand
        type: operand
  instruction_return:
    seq:
      - id: no_value
        size: 0
  instruction_new_typedesc:
    seq:
      - id: lhs_operand
        type: operand
      - id: type_cp_index
        type: s4
  instruction_new_structure:
    seq:
      - id: rhs_operand
        type: operand
      - id: lhs_operand
        type: operand
  operand:
    seq:
      - id: ignored_variable
        type: u1
      - id: ignored_type_cp_index
        type: s4
        if: ignored_variable == 1
      - id: variable
        type: variable
        if: ignored_variable == 0
  variable:
    seq:
      - id: kind
        type: s1
      - id: scope
        type: s1
      - id: variable_dcl_name_cp_index
        type: s4
      - id: global_or_constant_variable
        type: global_variable
        if: kind == 5 or kind == 7
  global_variable:
    seq:
      - id: package_index
        type: s4
      - id: type_cp_index
        type: s4

