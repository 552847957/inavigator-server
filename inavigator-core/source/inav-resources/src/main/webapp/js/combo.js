function cascadeSelect(parent, child) {
	var childOptions = child.find('option:not(.static)');
	child.data('options', childOptions);

	parent.change(function() {
		childOptions.remove();
		child.append(child.data('options').filter('.tag_' + this.value)).change();
	})

	childOptions.not('.static, .tag_' + parent.val()).remove();
}
