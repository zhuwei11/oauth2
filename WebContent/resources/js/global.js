		$(document).ready(function() {
			
			$("#check_box").click(function() {
				if (this.checked) {
					$("input[name='ids[]']").each(function() {
						this.checked = true;
					});
				} else {
					$("input[name='ids[]']").each(function() {
						this.checked = false;
					});
				}
			});
			
			$('#select-button').click(function() {
				if($(this).val() == 0) {
					$("input[name='ids[]']").each(function() {
						this.checked = true;
					});
					$(this).val(1);
					$(this).html("取消全选");
				} else {
					$("input[name='ids[]']").each(function() {
						this.checked = false;
					});
					$(this).val(0);
					$(this).html("全部选择");
				}
			});
		});
		
		
