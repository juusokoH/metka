define(function (require) {
    'use strict';

    return function treeView(root, events) {

        function recursiveForEach(f) {
            root.forEach(function recur(node) {
                if (node.children) {
                    node.children.forEach(recur);
                }
                f.apply(this, arguments);
            });
        }

        function $dirItems($root) {
            var atNextSiblingDir = false;
            var level = $root.data('level');
            return $root.nextAll().filter(function () {
                if (atNextSiblingDir) {
                    return false;
                }
                if (level < $(this).data('level')) {
                    return true;
                }
                atNextSiblingDir = true;
            });
        }

        function deactivateAll() {
            recursiveForEach(function (node) {
                node.active = false;
            });
            $div.children().removeClass('active');
        }

        function children(items, level) {
            return items.map(function (node) {

                var $icon = $('<span class="glyphicon">')
                    .addClass(node.children ? closed : 'glyphicon-file');

                var $a = $('<a class="list-group-item" href="javascript:void 0;">')
                    .toggleClass('active', !!node.active)
                    .append($icon)
                    .append(' ' + node.text)
                    .data({
                        level: level,
                        node: node
                    });
                $icon
                    .css('margin-left', (level * 16) + 'px');

                if (node.children) {
                    $icon.click(function () {
                        if ($icon.hasClass(closed)) {
                            // open

                            $a.after(children(node.children, level + 1));
                        } else {
                            // close

                            $dirItems($a).remove();
                        }
                        $icon.toggleClass(closed);
                        $icon.toggleClass(open);
                        return false;
                    });
                }

                return $a;
            });
        }

        var open = 'glyphicon-folder-open';
        var closed = 'glyphicon-folder-close';

        function activeNodes() {
            var response = [];
            recursiveForEach(function (node) {
                if (node.active) {
                    response.push(node);
                }
            });
            return response;
        }

        var $div = $('<div class="list-group list-group-condensed">')
            .append(children(root, 0))
            .on({
                activate: function () {
                    var $this = $(this);
                    $this
                        .addClass('active')
                        .data('node').active = true;

                    $this
                        .trigger('change');
                },
                deactivate: function () {
                    var $this = $(this);
                    $this
                        .removeClass('active')
                        .data('node').active = false;

                    $this
                        .trigger('change');
                },
                toggle: function () {
                    var $this = $(this);
                    $this
                        .toggleClass('active')
                        .data('node').active = $this.hasClass('active');

                    $this
                        .trigger('change');
                },
                deactivateDirectoriesAndToggle: function () {
                    var $this = $(this);
                    var nodeLevel = $this.data('level');

                    $this.siblings().filter(function () {
                        return !!$(this).data('node').children;
                    }).removeClass('active');

                    recursiveForEach(function (node) {
                        if (node.children) {
                            node.active = false;
                        }
                    });
                    $this.trigger('toggle');
                },
                activateOne: function () {
                    var $this = $(this);

                    deactivateAll();

                    $this.trigger('activate');
                }
            }, 'a')
            .data({
                remove: function () {
                    var removed = activeNodes();

                    // remove from DOM
                    $div.children('.active').remove();

                    //remove from data
                    root = root.filter(function recur(node) {
                        if (node.children) {
                            node.children = node.children.filter(recur);
                        }
                        return !node.active;
                    });

                    $div
                        .trigger('change');

                    return removed;
                },
                add: function (nodes) {
                    var addTo = root;
                    var path = [];

                    if (root.some(function recur(node, i, array) {
                        var found = (function () {
                            if (node.active) {
                                if (node.children) {
                                    addTo = node.children;
                                } else {
                                    addTo = array;
                                }
                                return true;
                            } else {
                                if (node.children) {
                                    return node.children.some(recur);
                                } else {
                                    return false;
                                }
                            }
                        })();
                        if (found && node.children) {
                            path.push(node);
                        }
                        return found;
                    })) {
                        deactivateAll();
                        Array.prototype.push.apply(addTo, nodes);
                        log(path)
                        if (path.length) {
                            path.reverse().forEach(function (node, i) {
                                var $icon = $div.children().filter(function () {
                                    return node === $(this).data('node');
                                }).children('span').first();

                                if ($icon.hasClass(closed)) {
                                    $icon.click();
                                } else {
                                    // if "folder" is already open, do "refresh"
                                    $icon.click().click();
                                }
                            });
                        } else {
                            $div.append(children(nodes, 0));
                        }
                    } else {
                        deactivateAll();
                        Array.prototype.push.apply(root, nodes);
                        $div.append(children(nodes, 0));
                    }

                    var container = $div,
                        scrollTo = $div.children('.active');

                    container.scrollTop(
                            scrollTo.offset().top - container.offset().top + container.scrollTop()
                    );

                    $div
                        .trigger('change');
                    return;
                },
                move: function move(to) {
                    to.data('add')($div.data('remove')());
                }
            });

        if (events.onClick) {
            $div.on('click', 'a', function () {
                var $this = $(this);
                $this.trigger(events.onClick($this.data('node')));
            });
        }

        if (events.onChange) {
            $div.on('change', function () {
                events.onChange(activeNodes());
            })
        }

        return $div;
    }
});